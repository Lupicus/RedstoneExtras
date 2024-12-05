var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI')
var opc = Java.type('org.objectweb.asm.Opcodes')
var AbstractInsnNode = Java.type('org.objectweb.asm.tree.AbstractInsnNode')
var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode')
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode')
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode')
var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode')
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode')

function initializeCoreMod() {
    return {
    	'redstoneWire': {
    		'target': {
    			'type': 'CLASS',
    			'name': 'net.minecraft.world.level.redstone.RedstoneWireEvaluator'
    		},
    		'transformer': function(classNode) {
    			var count = 0
    			var fn = "getIncomingWireSignal"
    			for (var i = 0; i < classNode.methods.size(); ++i) {
    				var obj = classNode.methods.get(i)
    				if (obj.name == fn) {
    					patch_calcTS(obj)
    					count++
    				}
    			}
    			if (count < 1)
    				asmapi.log("ERROR", "Failed to modify RedstoneWireBlock: Method not found")
    			return classNode;
    		}
    	}
    }
}

// replace first call (of 3) to getWireSignal with RedstonePipeBlock::getLineSignalHook
function patch_calcTS(obj) {
	var fn = "getWireSignal"
	var owner = "net/minecraft/world/level/redstone/RedstoneWireEvaluator"
	var desc = "(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)I"
	var node = asmapi.findFirstMethodCall(obj, asmapi.MethodType.VIRTUAL, owner, fn, desc)
	if (node) {
		var count = 1
		var index = obj.instructions.indexOf(node)
		while (true) {
			var node2 = asmapi.findFirstMethodCallAfter(obj, asmapi.MethodType.VIRTUAL, owner, fn, desc, index + 1)
			if (node2 == null) break
			count++
			index = obj.instructions.indexOf(node2)
		}
		if (count == 3)
		{
			var call2 = asmapi.buildMethodCall("com/lupicus/rsx/block/RedstonePipeBlock", "getLineSignalHook", desc, asmapi.MethodType.STATIC)
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
