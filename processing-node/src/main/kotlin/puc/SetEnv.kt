@file:Suppress("UNCHECKED_CAST")

package puc

import java.lang.reflect.Field
import java.util.Collections

object SetEnv {
    fun apply(newenv: Map<String, String>) {
        try {
            val processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment")
            val theEnvironmentField: Field = processEnvironmentClass.getDeclaredField("theEnvironment")
            theEnvironmentField.isAccessible = true
            val env: MutableMap<String, String> = theEnvironmentField.get(null) as MutableMap<String, String>
            env.putAll(newenv)
            val theCaseInsensitiveEnvironmentField: Field =
                processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment")
            theCaseInsensitiveEnvironmentField.isAccessible = true
            val cienv: MutableMap<String, String> =
                theCaseInsensitiveEnvironmentField.get(null) as MutableMap<String, String>
            cienv.putAll(newenv)
        } catch (e: NoSuchFieldException) {
            try {
                val classes = Collections::class.java.declaredClasses
                val env = System.getenv()
                for (cl in classes) {
                    if ("java.util.Collections\$UnmodifiableMap" == cl.name) {
                        val field: Field = cl.getDeclaredField("m")
                        field.isAccessible = true
                        val obj: Any = field.get(env)
                        val map: MutableMap<String, String> = obj as MutableMap<String, String>
                        map.putAll(newenv)
                    }
                }
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        } catch (e1: Exception) {
            e1.printStackTrace()
        }
    }
}