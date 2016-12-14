package com.hubspot.jackson.datatype.protobuf;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.*;
import com.google.protobuf.Descriptors;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static java.util.Locale.ENGLISH;

public class ProtobufPropertiesCollector extends POJOPropertiesCollector {
    protected ProtobufPropertiesCollector(MapperConfig<?> config, boolean forSerialization, JavaType type, AnnotatedClass classDef, String mutatorPrefix) {
        super(config, forSerialization, type, classDef, mutatorPrefix);
    }

    @Override
    protected void _addFields(Map<String, POJOPropertyBuilder> props) {
        Class clazz = _type.getRawClass();
        Descriptors.Descriptor descriptor;

        try {
            descriptor = (Descriptors.Descriptor) clazz.getDeclaredMethod("getDescriptor").invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("Unable to invoke getDescriptor() for type " + clazz, e);
        }

        for (Descriptors.FieldDescriptor field : descriptor.getFields()) {
            addProperty(props, field, clazz);
        }
    }

    /**
     *
     * @param props
     * @param field
     * @param clazz class to introspect
     */
    protected void addProperty(Map<String, POJOPropertyBuilder> props, Descriptors.FieldDescriptor field, Class clazz) {
        PropertyName pn = new PropertyName(field.getName());
        try {
            String fieldGetter;

            if (field.isRepeated() && ! field.isMapField())
                fieldGetter = "get" + capitalize(field.getName()) + "List";
            else
                fieldGetter = "get" + capitalize(field.getName());

            String fieldSetter = "set" + capitalize(field.getName());
            Method setterMethod = null;

            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equals(fieldSetter)) {
                    setterMethod = m;
                    break;
                }
            }

            _property(props, pn).addGetter(
                    new AnnotatedMethod(_classDef, clazz.getMethod(fieldGetter), null, null),
                    pn,
                    true, true, false);

            if (setterMethod != null)
                _property(props, pn).addSetter(
                        new AnnotatedMethod(_classDef, setterMethod, null, null),
                        pn,
                        true, true, false);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot introspect " + clazz, e);
        }
    }

    /**
     * Returns a String which capitalizes the first letter of the string.
     */
    public static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
    }

    @Override
    protected void _addCreators(Map<String, POJOPropertyBuilder> props) {
    }

    @Override
    protected void _addMethods(Map<String, POJOPropertyBuilder> props) {
    }

    @Override
    protected void _addInjectables(Map<String, POJOPropertyBuilder> props) {
    }

    @Override
    public List<BeanPropertyDefinition> getProperties() {
        return super.getProperties();
    }
}
