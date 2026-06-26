package com.weinsim.slpaint.renderengine.bufferobjects;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjglx.util.vector.Matrix2f;
import org.lwjglx.util.vector.Matrix3f;
import org.lwjglx.util.vector.Matrix4f;
import org.lwjglx.util.vector.Vector2f;
import org.lwjglx.util.vector.Vector3f;
import org.lwjglx.util.vector.Vector4f;

public class UBOEntry {

    private ByteBuffer buffer;

    /**
     * 
     * @param capacity Buffer capacity in bytes.
     */
    public UBOEntry(int size) {
        buffer = BufferUtils.createByteBuffer(size);
    }

    public void put(Object data) {
        // Compacting this switch expression (see below) does not work because it
        // produces a VerifyError. See bottom of the file for the full error message or
        // https://chatgpt.com/c/69864e48-fb0c-8326-ad68-26af8652a274
        // for an explanation.
        // Edit: still not fixed in java 26

        VBO.put(data, switch (data) {
            case Float _ -> 1;
            case Integer _ -> 1;
            case Double _ -> 1;
            case Vector2f _ -> 2;
            case Matrix2f _ -> 2;
            case Vector3f _ -> 3;
            case Matrix3f _ -> 3;
            case Vector4f _ -> 4;
            case Matrix4f _ -> 4;
            default -> 0;
        }, buffer, true);

        // VBO.put(data, switch (data) {
        // case Float _,Integer _,Double _ -> 1;
        // case Vector2f _,Matrix2f _ -> 2;
        // case Vector3f _,Matrix3f _ -> 3;
        // case Vector4f _,Matrix4f _ -> 4;
        // default -> 0;
        // }, buffer, true);
    }

    public void put(Object data, int coordinateSize) {
        VBO.put(data, coordinateSize, buffer, true);
    }

    public void putPadding(int numBytes) {
        VBO.putPadding(numBytes, buffer);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj instanceof UBOEntry e)
            return buffer.equals(e.buffer);

        return false;
    }

    public UBOEntry finish() {
        if (buffer.hasRemaining())
            throw new RuntimeException("UBO is not filled!");

        buffer.flip();

        return this;
    }

    public int size() {
        return buffer.capacity();
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
}

/**
 * Full error message from put() method:
 * 
 * <pre>
 * Exception in thread "main" java.lang.VerifyError: Inconsistent stackmap frames at branch target 116
Exception Details:
  Location:
    renderengine/bufferobjects/UBOEntry.put(Ljava/lang/Object;)V @108: goto
  Reason:
    Current frame's stack size doesn't match stackmap.
  Current Frame:
    bci: @108
    flags: { }
    locals: { 'renderengine/bufferobjects/UBOEntry', 'java/lang/Object', 'java/lang/Object' }
    stack: { }
  Stackmap Frame:
    bci: @116
    flags: { }
    locals: { 'renderengine/bufferobjects/UBOEntry', 'java/lang/Object', 'java/lang/Object' }
    stack: { 'java/lang/Object' }
  Bytecode:
    0000000: 2b2b 59b8 001d 574d 2c03 ba00 2300 00aa
    0000010: 0000 00ec 0000 0000 0000 0008 0000 0031
    0000020: 0000 0031 0000 0031 0000 0069 0000 0069
    0000030: 0000 0094 0000 0094 0000 00c0 0000 00c0
    0000040: 2c59 c100 2799 0009 5957 57a7 0029 59c1
    0000050: 0029 9900 0959 5757 a700 1c59 c100 2b99
    0000060: 0009 5957 57a7 000f 57a7 0006 a700 082c
    0000070: 06a7 ff99 04a7 0087 2c59 c100 2d99 0009
    0000080: 5957 57a7 001c 59c1 002f 9900 0959 5757
    0000090: a700 0f57 a700 06a7 0008 2c08 a7ff 6e05
    00000a0: a700 5c2c 59c1 0031 9900 0959 5757 a700
    00000b0: 1d59 c100 3399 0009 5957 57a7 0010 57a7
    00000c0: 0006 a700 092c 1007 a7ff 4206 a700 302c
    00000d0: 59c1 0035 9900 0959 5757 a700 1d59 c100
    00000e0: 3799 0009 5957 57a7 0010 57a7 0006 a700
    00000f0: 092c 1009 a7ff 1607 a700 0403 2ab4 0013
    0000100: 04b8 0039 b1                           
  Stackmap Table:
    full_frame(@10,{Object[#1],Object[#3],Object[#3]},{Object[#3],Object[#3],Integer})
    same_locals_1_stack_item_frame(@64,Object[#3])
    full_frame(@78,{Object[#1],Object[#3],Object[#3]},{Object[#3],Object[#3]})
    full_frame(@91,{Object[#1],Object[#3],Object[#3]},{Object[#3],Object[#3]})
    full_frame(@104,{Object[#1],Object[#3],Object[#3]},{Object[#3],Object[#3]})
    same_frame(@108)
    same_locals_1_stack_item_frame(@111,Object[#3])
    same_locals_1_stack_item_frame(@116,Object[#3])
    same_locals_1_stack_item_frame(@120,Object[#3])
    full_frame(@134,{Object[#1],Object[#3],Object[#3]},{Object[#3],Object[#3]})
    full_frame(@147,{Object[#1],Object[#3],Object[#3]},{Object[#3],Object[#3]})
    same_frame(@151)
    same_locals_1_stack_item_frame(@154,Object[#3])
    same_locals_1_stack_item_frame(@159,Object[#3])
    same_locals_1_stack_item_frame(@163,Object[#3])
    full_frame(@177,{Object[#1],Object[#3],Object[#3]},{Object[#3],Object[#3]})
    full_frame(@190,{Object[#1],Object[#3],Object[#3]},{Object[#3],Object[#3]})
    same_frame(@194)
    same_locals_1_stack_item_frame(@197,Object[#3])
    same_locals_1_stack_item_frame(@203,Object[#3])
    same_locals_1_stack_item_frame(@207,Object[#3])
    full_frame(@221,{Object[#1],Object[#3],Object[#3]},{Object[#3],Object[#3]})
    full_frame(@234,{Object[#1],Object[#3],Object[#3]},{Object[#3],Object[#3]})
    same_frame(@238)
    same_locals_1_stack_item_frame(@241,Object[#3])
    same_locals_1_stack_item_frame(@247,Object[#3])
    same_locals_1_stack_item_frame(@251,Object[#3])
    full_frame(@252,{Object[#1],Object[#3]},{Object[#3],Integer})

        at renderengine.drawcalls.RectDrawCall.getGroupAttributes(RectDrawCall.java:40)
        at renderengine.renderers.ShapeRenderer.addShape(ShapeRenderer.java:35)
        at renderengine.UIRenderMaster.rect(UIRenderMaster.java:205)
        at renderengine.AppRenderer.drawShape(AppRenderer.java:259)
        at renderengine.AppRenderer.renderUIElement(AppRenderer.java:141)
        at renderengine.AppRenderer.renderUI(AppRenderer.java:88)
        at renderengine.AppRenderer.render(AppRenderer.java:72)
        at main.apps.App.render(App.java:296)
        at main.MainLoop.main(MainLoop.java:56)
 * </pre>
 */
