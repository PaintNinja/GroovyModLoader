/*
 * MIT License
 *
 * Copyright (c) 2022 matyrobbrt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.matyrobbrt.gml.transform.gmods

import com.matyrobbrt.gml.GMLModLoadingContext
import com.matyrobbrt.gml.bus.GModEventBus
import com.matyrobbrt.gml.transform.TransformationUtils
import com.matyrobbrt.gml.transform.api.GModTransformer
import groovy.transform.CompileStatic
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.IEventBus
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.tools.GeneralUtils
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.trait.Traits
import org.objectweb.asm.Opcodes

@CompileStatic
final class BusTransformer implements GModTransformer {
    @Override
    void transform(ClassNode classNode, AnnotationNode annotationNode, SourceUnit source) {
        final modBus = classNode.addField(
                'modBus', Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, ClassHelper.make(GModEventBus),
                GeneralUtils.callX(GeneralUtils.callX(ClassHelper.make(GMLModLoadingContext), 'get'), 'getModEventBus')
        )
        getOrCreateMethod(classNode, 'getModBus', modBus.type).setCode(
                GeneralUtils.returnS(GeneralUtils.fieldX(modBus))
        )

        final forgeBus = classNode.addField(
                'forgeBus', Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, ClassHelper.make(IEventBus),
                GeneralUtils.propX(GeneralUtils.classX(MinecraftForge), 'EVENT_BUS')
        )
        getOrCreateMethod(classNode, 'getForgeBus', forgeBus.type).setCode(
                GeneralUtils.returnS(GeneralUtils.fieldX(forgeBus))
        )
    }

    private static MethodNode getOrCreateMethod(ClassNode clazz, String name, ClassNode type) {
        return (clazz.getMethod(name, Parameter.EMPTY_ARRAY)?.tap {
            it.annotations.removeIf { it.classNode == Traits.TRAITBRIDGE_CLASSNODE }
        } ?: clazz.addMethod(
                name, Opcodes.ACC_PUBLIC, type, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null
        )).tap {
            it.addAnnotation(TransformationUtils.GENERATED_ANNOTATION)
        }
    }
}
