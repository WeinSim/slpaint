package com.weinsim.slpaint.renderengine.shader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.weinsim.slpaint.main.Loader;
import com.weinsim.slpaint.renderengine.Cleanable;
import com.weinsim.slpaint.renderengine.RawModel;
import com.weinsim.slpaint.renderengine.bufferobjects.*;

public class ShaderProgram implements Cleanable {

    private final String name;
    private final ShaderType type;

    private final int programID;
    private final int vertexShaderID;
    private final int geometryShaderID;
    private final int fragmentShaderID;

    private final RawModel rawModel;

    private final HashMap<String, UniformVariable> uniformVariables;
    private final HashMap<String, UniformBufferObject> uniformBufferObjects;

    /**
     * @param name
     * @param type
     * @param vertexVBONames A list of names of vertex shader inputs that come from
     *                       per-vertex VBOs. All other inputs are considered to
     *                       come from per-instance VBOs.
     */
    public ShaderProgram(String name, ShaderType type, List<String> vertexVBONames) {
        this.type = type;
        this.name = name;

        uniformVariables = new HashMap<>();
        uniformBufferObjects = new HashMap<>();

        ArrayList<AttributeVBO> vbos = new ArrayList<>();
        boolean hasGeometry = type.hasGeometry();
        String vertexName, geometryName = null, fragmentName;

        vertexName = getShaderStageName(type.vertexPath, name);
        if (hasGeometry)
            geometryName = getShaderStageName(type.geometryPath, name);
        fragmentName = getShaderStageName(type.fragmentPath, name);

        vertexShaderID = loadShader(vertexName, GL_VERTEX_SHADER, vbos, vertexVBONames);
        geometryShaderID = hasGeometry ? loadShader(geometryName, GL_GEOMETRY_SHADER) : 0;
        fragmentShaderID = loadShader(fragmentName, GL_FRAGMENT_SHADER);

        programID = glCreateProgram();

        glAttachShader(programID, vertexShaderID);
        if (hasGeometry)
            glAttachShader(programID, geometryShaderID);
        glAttachShader(programID, fragmentShaderID);

        int attributeNumber = 0;
        for (AttributeVBO vbo : vbos) {
            glBindAttribLocation(programID, attributeNumber, vbo.attributeName());
            attributeNumber += vbo.getNumAttributes();
        }
        rawModel = new RawModel(vbos);

        glLinkProgram(programID);
        if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
            System.out.println(glGetProgramInfoLog(programID));
            throw new RuntimeException(String.format("Could not link shader \"%s\"!\n", name));
        }
        glValidateProgram(programID);
        if (glGetProgrami(programID, GL_VALIDATE_STATUS) == GL_FALSE) {
            System.out.println(glGetProgramInfoLog(programID));
            throw new RuntimeException(String.format("Could not validate shader \"%s\"!\n", name));
        }

        for (UniformVariable uniform : uniformVariables.values())
            uniform.setLocation(glGetUniformLocation(programID, uniform.getName()));
    }

    private static String getShaderStageName(String format, Object... args) {
        return format.contains("%") ? String.format(format, args) : format;
    }

    public void start() {
        glUseProgram(programID);
    }

    public void stop() {
        glUseProgram(0);
    }

    @Override
    public void cleanUp() {
        stop();

        boolean hasGeometry = type.hasGeometry();

        glDetachShader(programID, vertexShaderID);
        if (hasGeometry)
            glDetachShader(programID, geometryShaderID);
        glDetachShader(programID, fragmentShaderID);

        glDeleteShader(vertexShaderID);
        if (hasGeometry)
            glDeleteShader(geometryShaderID);
        glDeleteShader(fragmentShaderID);

        glDeleteProgram(programID);
    }

    public void loadUniform(String name, Object value) {
        UniformVariable uniform = uniformVariables.get(name);

        if (uniform == null)
            throw new RuntimeException(
                    String.format("Shader \"%s\": uniform variable \"%s\" doesn't exist!", this.name, name));

        uniform.load(value);
    }

    public void loadUBOData(String uboName, ByteBuffer buffer) {
        UniformBufferObject groupAttributes = getUniformBlock(uboName);
        groupAttributes.setData(buffer);
        groupAttributes.syncData();
    }

    private UniformBufferObject getUniformBlock(String name) {
        UniformBufferObject ubo = uniformBufferObjects.get(name);

        if (ubo == null)
            throw new RuntimeException(String.format("Uniform block \"%s\" doesn't exist", name));

        return ubo;
    }

    public RawModel getRawModel() {
        return rawModel;
    }

    private int loadShader(String filename, int type) {
        return loadShader(filename, type, null, List.of());
    }

    private int loadShader(String filename, int type, ArrayList<AttributeVBO> vbos, List<String> vertexVBONames) {
        StringBuilder shaderSource = new StringBuilder();
        int attributeNumber = 0;

        String[] allLines;
        try {
            allLines = Loader.getString(filename).split("\n");
        } catch (IOException e) {
            throw new RuntimeException(String.format("Unable to read file \"%s\"", filename));
        }

        boolean insideUBO = false;
        String uboName = null;
        int uboBinding = 0;
        for (String line : allLines) {
            shaderSource.append(line).append("\n");

            // ignore comments
            String trimmed = line.trim();
            if (trimmed.startsWith("//"))
                continue;

            // finish UBO
            if (insideUBO) {
                if (line.contains("}")) {
                    UniformBufferObject ubo = new UniformBufferObject(uboName, uboBinding);
                    uniformBufferObjects.put(uboName, ubo);
                    insideUBO = false;
                }
            }

            // attributes
            if (vbos != null) {
                String[] parts = line.split(" ");
                int inIndex = -1;
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].equals("in")) {
                        inIndex = i;
                        break;
                    }
                }
                if (inIndex != -1) {
                    String attributeName = parts[inIndex + 2];
                    attributeName = attributeName.substring(0, attributeName.indexOf(';'));
                    // very crude detection for now
                    // VBOType attributeType = switch (attributeName) {
                    // case "cornerPos", "offset" -> VBOType.VERTEX;
                    // default -> VBOType.INSTANCE;
                    // };
                    VBOType attributeType = vertexVBONames.contains(attributeName) ? VBOType.VERTEX : VBOType.INSTANCE;
                    Datatype datatype = Datatype.fromIdentifier(parts[inIndex + 1]);
                    AttributeVBO vbo = switch (datatype) {
                        case INT ->
                            new IntVBO(attributeName, attributeNumber, datatype.coordinateSize, attributeType);
                        case FLOAT, VEC2, VEC3, VEC4 ->
                            new FloatVBO(attributeName, attributeNumber, datatype.coordinateSize, attributeType);
                        case MAT2, MAT3, MAT4 ->
                            new MatrixVBO(attributeName, attributeNumber, datatype.coordinateSize, attributeType);
                        default -> throw new RuntimeException("Invalid attribute datatype: " + parts[1]);
                    };
                    attributeNumber += vbo.getNumAttributes();
                    vbos.add(vbo);
                }
            }

            // uniform variables
            // uniform blocks
            int uniformIndex = line.indexOf("uniform");
            if (uniformIndex != -1) {
                if (line.contains("{")) {
                    // uniform buffer object
                    insideUBO = true;
                    uboName = line.substring(uniformIndex + 8, line.length() - 2);
                    int bindingStrIndex = line.indexOf("binding = ");
                    if (bindingStrIndex == -1) {
                        System.err.println("Couldn't find UBO binding!");
                        continue;
                    }
                    // (only works for bindings between 0 and 9)
                    uboBinding = (int) (line.charAt(bindingStrIndex + 10) - '0');
                } else {
                    // normal uniform variable
                    String[] parts = line.split(" ");
                    Datatype datatype = Datatype.fromIdentifier(parts[1]);
                    if (datatype == null) {
                        System.err.format("Invalid datatype: \"%s\"!\n", parts[1]);
                        continue;
                    }
                    String name = parts[2].replaceAll(";", "");
                    int openIndex = name.indexOf('[');
                    if (openIndex != -1) {
                        int closeIndex = name.indexOf(']');
                        String baseName = name.substring(0, openIndex);
                        int len = Integer.parseInt(name.substring(openIndex + 1, closeIndex));
                        for (int i = 0; i < len; i++) {
                            name = String.format("%s[%d]", baseName, i);
                            uniformVariables.put(name, new UniformVariable(datatype, name));
                        }
                    } else {
                        uniformVariables.put(name, new UniformVariable(datatype, name));
                    }
                }
            }
        }

        int shaderID = glCreateShader(type);

        glShaderSource(shaderID, shaderSource);
        glCompileShader(shaderID);
        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE) {
            System.out.println(glGetShaderInfoLog(shaderID));
            throw new RuntimeException(String.format("Unable to compile shader \"%s\"", filename));
        }
        return shaderID;
    }

}
