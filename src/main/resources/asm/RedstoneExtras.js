var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI')
var opc = Java.type('org.objectweb.asm.Opcodes')
var AbstractInsnNode = Java.type('org.objectweb.asm.tree.AbstractInsnNode')
var Label = Java.type('org.objectweb.asm.tree.LabelNode')
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode')
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode')
var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode')
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode')

function initializeCoreMod() {
    return {
    	'RedstoneWire': {
    		'target': {
    			'type': 'CLASS',
    			'name': 'net.minecraft.block.RedstoneWireBlock'
    		},
    		'transformer': function(classNode) {
    			var count = 0
    			var fn = asmapi.mapMethod('func_212568_b') // func_212568_b
    			var fn2 = 'canConnectTo'
    			for (var i = 0; i < classNode.methods.size(); ++i) {
    				var obj = classNode.methods.get(i)
    				if (obj.name == fn) {
    					patch_func_212568_b(obj)
    					count++
    				}
    				else if (obj.name == fn2) {
    					patch_canConnectTo(obj)
    					count++
    				}
    			}
    			if (count < 2)
    				asmapi.log("ERROR", "Failed to modify RedstoneWireBlock: Method not found")
    			return classNode;
    		}
    	}
    }
}

// replace first call (of 3) to maxSignal with RedstonePipeBlock::maxSignalHook
function patch_func_212568_b(obj) {
	var fn = asmapi.mapMethod('func_212567_a') // maxSignal
	var owner = "net/minecraft/block/RedstoneWireBlock"
	var desc = "(ILnet/minecraft/block/BlockState;)I"
	var node = asmapi.findFirstMethodCall(obj, asmapi.MethodType.SPECIAL, owner, fn, desc)
	if (node) {
		var count = 1
		var index = obj.instructions.indexOf(node)
		while (true) {
			var node2 = asmapi.findFirstMethodCallAfter(obj, asmapi.MethodType.SPECIAL, owner, fn, desc, index + 1)
			if (node2 == null) break
			count++
			index = obj.instructions.indexOf(node2)
		}
		if (count == 3)
		{
			var call2 = asmapi.buildMethodCall("com/lupicus/rsx/block/RedstonePipeBlock", "maxSignalHook", desc, asmapi.MethodType.STATIC)
			obj.instructions.remove(node.getPrevious().getPrevious().getPrevious())  // remove this argument
			obj.instructions.insertBefore(node, call2)
			obj.instructions.remove(node)
		}
		else
			asmapi.log("ERROR", "Failed to modify RedstoneWireBlock: bad call count; already modified?")
	}
	else
		asmapi.log("ERROR", "Failed to modify RedstoneWireBlock: call not found")
}

// add the test: if (block == ModBlocks.BLUESTONE_WIRE) return false;
function patch_canConnectTo(obj) {
	var wire = asmapi.mapField('field_150488_af')
	var node = asmapi.findFirstInstruction(obj, opc.GETSTATIC)
	if (node && node.name == wire) {
		node2 = node
		for (var i = 0; i < 6; ++i)
		{
			node2 = node2.getNext()
			if (node2 == null)
				break
		}
		if (node2 && node2.getType() == AbstractInsnNode.LABEL)
		{
			var op6 = new Label()
			var op1 = new VarInsnNode(opc.ALOAD, 4)
			var op2 = new FieldInsnNode(opc.GETSTATIC, "com/lupicus/rsx/block/ModBlocks", "BLUESTONE_WIRE", "Lnet/minecraft/block/Block;")
			var op3 = new JumpInsnNode(opc.IF_ACMPNE, op6)
			var op4 = new InsnNode(opc.ICONST_0)
			var op5 = new InsnNode(opc.IRETURN)
			var list = asmapi.listOf(op1, op2, op3, op4, op5, op6)
			obj.instructions.insert(node2, list)
		}
		else
			asmapi.log("ERROR", "Failed to modify RedstoneWireBlock: Label not found")
	}
	else
		asmapi.log("ERROR", "Failed to modify RedstoneWireBlock: GETSTATIC not found")
}
