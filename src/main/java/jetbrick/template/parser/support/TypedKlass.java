package jetbrick.template.parser.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import jetbrick.template.runtime.*;

/*
 * 泛型类描述
 * String -> {klass: String.class, types: []}
 * List<String> -> {klass: List.class, types: [{klass: String.class, types: []}]}
 */
public class TypedKlass {
    private static final ConcurrentMap<Class<?>, TypedKlass> cache = new ConcurrentHashMap<Class<?>, TypedKlass>();

    public static final TypedKlass[] EMPTY_TYPE_ARGS = new TypedKlass[0];
    public static final TypedKlass NULL = new TypedKlass(null, EMPTY_TYPE_ARGS);
    public static final TypedKlass WildcharTypedKlass = new TypedKlass(Object.class, EMPTY_TYPE_ARGS);
    public static final TypedKlass Object = create(Object.class);
    public static final TypedKlass JetForStatus = create(JetForStatus.class);
    public static final TypedKlass JetWriter = create(JetWriter.class);
    public static final TypedKlass JetContext = create(JetContext.class);

    private final Class<?> klass;
    private final TypedKlass[] typeArgs; // 存储的泛型信息

    public static TypedKlass create(Class<?> klass) {
        return create(klass, EMPTY_TYPE_ARGS);
    }

    public static TypedKlass create(Class<?> klass, TypedKlass[] typeArgs) {
        if (klass == null) return TypedKlass.NULL;

        TypedKlass typedKlass;
        if (typeArgs == null) {
            typeArgs = EMPTY_TYPE_ARGS;
        }
        if (typeArgs.length == 0) {
            // 只给非泛型的对象进行缓存
            typedKlass = cache.get(klass);
            if (typedKlass == null) {
                typedKlass = new TypedKlass(klass, typeArgs);
                // 多线程冲突的时候，使用老的对象，新建的对象将被垃圾回收
                TypedKlass old = cache.putIfAbsent(klass, typedKlass);
                if (old != null) typedKlass = old;
            }
        } else {
            typedKlass = new TypedKlass(klass, typeArgs);
        }
        return typedKlass;
    }

    private TypedKlass(Class<?> klass, TypedKlass[] typeArgs) {
        this.klass = klass;
        this.typeArgs = typeArgs;
    }

    public Class<?> getKlass() {
        return klass;
    }

    public TypedKlass[] getTypeArgs() {
        return typeArgs;
    }

    public boolean isPrimitive() {
        return klass != null && klass.isPrimitive();
    }

    public TypedKlass asBoxedTypedKlass() {
        if (isPrimitive()) {
            Class<?> toklass = PrimitiveUtils.asBoxedClass(klass);
            return create(toklass);
        } else {
            return this;
        }
    }

    public String getSource() {
        StringBuilder sb = new StringBuilder();

        Class<?> toklass = klass;

        // 处理数组
        String arraySuffix = ""; // 数组维度
        while (toklass.isArray()) {
            toklass = toklass.getComponentType();
            arraySuffix += "[]";
        }

        // 使用短名字
        String name = toklass.getName();
        if (name.startsWith("java.lang.")) {
            name = name.substring("java.lang.".length());
        } else if (name.startsWith("java.util.")) {
            name = name.substring("java.util.".length());
        }
        name = name.replace('$', '.'); // 转换内部类名字
        sb.append(name);

        // 处理泛型内容
        if (typeArgs != null && typeArgs.length > 0) {
            sb.append('<');
            for (int i = 0; i < typeArgs.length; i++) {
                if (i > 0) sb.append(',');
                if (typeArgs[i] == WildcharTypedKlass) {
                    sb.append('?');
                } else {
                    sb.append(typeArgs[i].getSource());
                }
            }
            sb.append('>');
        }

        sb.append(arraySuffix);

        return sb.toString();
    }

    @Override
    public String toString() {
        return getSource();
    }
}