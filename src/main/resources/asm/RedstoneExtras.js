var asmapi = Java.type('net.minecraftforge.coremod.api.ASMAPI')

function initializeCoreMod() {
    return {
    	'RedstoneWire': {
    		'target': {
    			'type': 'CLASS',
    			'name': 'net.minecraft.block.RedstoneWireBlock'
    		},
    		'transformer': function(classNode) {
    			var fn = asmapi.mapMethod('func_212568_b') // func_212568_b
    			for (var i = 0; i < classNode.methods.size(); ++i) {
    				var obj = classNode.methods.get(i)
    				if (obj.name == fn)
    					patch_func_212568_b(obj)
    			}
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
