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
    	'redstoneWire': {
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
    	},
    	'redstoneDiode': {
    		'target': {
    			'type': 'CLASS',
    			'name': 'net.minecraft.block.RedstoneDiodeBlock'
    		},
    		'transformer': function(classNode) {
    			var count = 0
    			var fn = asmapi.mapMethod('func_176397_f') // calculateInputStrength
    			var fn2 = asmapi.mapMethod('func_176401_c') // getPowerOnSide
    			for (var i = 0; i < classNode.methods.size(); ++i) {
    				var obj = classNode.methods.get(i)
    				if (obj.name == fn) {
    					patch_func_176397_f(obj)
    					count++
    				}
    				else if (obj.name == fn2) {
    					patch_func_176401_c(obj)
    					count++
    				}
    			}
    			if (count < 2)
    				asmapi.log("ERROR", "Failed to modify RedstoneDiodeBlock: Method not found")
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

// calculateInputStrength
// add || block == ModBlocks.BLUESTONE_WIRE then blockstate.get(POWER)
function patch_func_176397_f(obj) {
	var wire = asmapi.mapField('field_150488_af')
	var node = asmapi.findFirstInstruction(obj, opc.GETSTATIC)
	while (node && node.name != wire) {
		var index = obj.instructions.indexOf(node)
		var node = asmapi.findFirstInstructionAfter(obj, opc.GETSTATIC, index + 1)
	}
	if (node) {
		node2 = node.getNext()
		if (node2 && node2.getOpcode() == opc.IF_ACMPNE)
		{
			obj.instructions.insertBefore(node, new InsnNode(opc.DUP))

			var lb1 = new Label()
			var op1 = new JumpInsnNode(opc.IF_ACMPEQ, lb1)
			var op2 = new FieldInsnNode(opc.GETSTATIC, "com/lupicus/rsx/block/ModBlocks", "BLUESTONE_WIRE", "Lnet/minecraft/block/Block;")
			var list = asmapi.listOf(op1, op2)
			obj.instructions.insert(node, list)

			op1 = new InsnNode(opc.ICONST_0)
			op2 = new InsnNode(opc.POP)
			list = asmapi.listOf(op1, lb1, op2)
			obj.instructions.insert(node2, list)
		}
		else
			asmapi.log("ERROR", "Failed to modify RedstoneDiodeBlock: Label not found")
	}
	else
		asmapi.log("ERROR", "Failed to modify RedstoneDiodeBlock: GETSTATIC not found")
}

// getPowerOnSide
// add || block == ModBlocks.BLUESTONE_WIRE then blockstate.get(POWER)
function patch_func_176401_c(obj) {
	var wire = asmapi.mapField('field_150488_af')
	var node = asmapi.findFirstInstruction(obj, opc.GETSTATIC)
	while (node && node.name != wire) {
		var index = obj.instructions.indexOf(node)
		var node = asmapi.findFirstInstructionAfter(obj, opc.GETSTATIC, index + 1)
	}
	if (node) {
		node2 = node.getNext()
		if (node2 && node2.getOpcode() == opc.IF_ACMPNE)
		{
			var op4 = new Label()
			var op1 = new JumpInsnNode(opc.IF_ACMPEQ, op4)
			var op2 = new VarInsnNode(opc.ALOAD, 5)
			var op3 = new FieldInsnNode(opc.GETSTATIC, "com/lupicus/rsx/block/ModBlocks", "BLUESTONE_WIRE", "Lnet/minecraft/block/Block;")
			var list = asmapi.listOf(op1, op2, op3)
			obj.instructions.insert(node, list)
			obj.instructions.insert(node2, op4)
		}
		else
			asmapi.log("ERROR", "Failed to modify RedstoneDiodeBlock: Label not found")
	}
	else
		asmapi.log("ERROR", "Failed to modify RedstoneDiodeBlock: GETSTATIC not found")
}
