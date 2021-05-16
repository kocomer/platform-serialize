package com.kocomer.serialize;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * 序列化
 */
public class SerializeUtils {
    /**
     * 提取对象key 和 class
     *
     * @param clazz
     * @return
     */
    private static final List<Helper> list(Class<?> clazz) {

        Field[] fields = clazz.getDeclaredFields();
        List<Helper> list = new ArrayList<>();
        for (Field field : fields) {
            Helper helper = new Helper();
            helper.name = field.getName();

            helper.clazz = field.getType();
            list.add(helper);
        }
        //根据名称排序
        Collections.sort(list, new Comparator<Helper>() {
            @Override
            public int compare(Helper o1, Helper o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        return list;
    }

    /**
     * 序列化
     *
     * @param obj
     * @return
     * @throws Exception
     */
    public static final byte[] encode(Object obj) throws Exception {
        Class<?> clazz = obj.getClass();
        List<Helper> list = list(clazz);


        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (Helper helper : list) {

            Field field = clazz.getDeclaredField(helper.name);
            field.setAccessible(true);
            Object value = field.get(obj);
            if (helper.clazz.equals(byte.class) || helper.clazz.equals(Byte.class)) {
                bos.write((byte) value);
            } else if (helper.clazz.equals(boolean.class) || helper.clazz.equals(Boolean.class)) {
                bos.write(((boolean) value) ? 0b00000001 : 0b00000000);
            } else if (helper.clazz.equals(short.class) || helper.clazz.equals(Short.class)) {
                Short tempValue = (Short) value;
                bos.write((byte) ((tempValue & 0xff00) >> 8));
                bos.write((byte) (tempValue & 0x00ff));
            } else if (helper.clazz.equals(char.class) || helper.clazz.equals(Character.class)) {
                Character tempValue = (Character) value;
                bos.write((byte) ((tempValue & 0xff00) >> 8));
                bos.write((byte) (tempValue & 0x00ff));
            } else if (helper.clazz.equals(int.class) || helper.clazz.equals(Integer.class)) {
                Integer tempValue = (Integer) value;

                bos.write((byte) ((tempValue & 0xff000000) >> 24));
                bos.write((byte) ((tempValue & 0x00ff0000) >> 16));
                bos.write((byte) ((tempValue & 0x0000ff00) >> 8));
                bos.write((byte) (tempValue & 0x000000ff));
            } else if (helper.clazz.equals(float.class) || helper.clazz.equals(Float.class)) {
                int tempValue = Float.floatToIntBits((float) value);

                bos.write((byte) ((tempValue & 0xff000000) >> 24));
                bos.write((byte) ((tempValue & 0x00ff0000) >> 16));
                bos.write((byte) ((tempValue & 0x0000ff00) >> 8));
                bos.write((byte) (tempValue & 0x000000ff));

            } else if (helper.clazz.equals(long.class) || helper.clazz.equals(Long.class)) {
                long tempValue = (long) value;

                bos.write((byte) ((tempValue & 0xff000000) >> 56));
                bos.write((byte) ((tempValue & 0x00ff0000) >> 48));
                bos.write((byte) ((tempValue & 0x0000ff00) >> 40));
                bos.write((byte) (tempValue & 0x000000ff >> 32));
                bos.write((byte) ((tempValue & 0xff000000) >> 24));
                bos.write((byte) ((tempValue & 0x00ff0000) >> 16));
                bos.write((byte) ((tempValue & 0x0000ff00) >> 8));
                bos.write((byte) (tempValue & 0x000000ff));
            } else if (helper.clazz.equals(double.class) || helper.clazz.equals(Double.class)) {
                long tempValue = Double.doubleToLongBits((double) value);

                bos.write((byte) ((tempValue & 0xff000000) >> 56));
                bos.write((byte) ((tempValue & 0x00ff0000) >> 48));
                bos.write((byte) ((tempValue & 0x0000ff00) >> 40));
                bos.write((byte) (tempValue & 0x000000ff >> 32));
                bos.write((byte) ((tempValue & 0xff000000) >> 24));
                bos.write((byte) ((tempValue & 0x00ff0000) >> 16));
                bos.write((byte) ((tempValue & 0x0000ff00) >> 8));
                bos.write((byte) (tempValue & 0x000000ff));
            } else if (helper.clazz.equals(String.class)) {
                String tempValue = (String) value;
                bos.write(tempValue.getBytes());
                bos.write(0x03);
            }
        }

        return bos.toByteArray();
    }

    /**
     * 反序列化
     *
     * @param data
     * @param <T>
     * @return
     */
    public static final <T> T decode(byte[] data, Class<?> clazz) throws Exception {
        ByteBuffer bf = ByteBuffer.allocate(data.length).put(data);
        bf.flip();

        T result = (T) clazz.newInstance();
        List<Helper> lists = list(clazz);
        for (int i = 0, length = lists.size(); i < length; i++) {
            Helper helper = lists.get(i);

            Field field = clazz.getDeclaredField(helper.name);
            field.setAccessible(true);

            if (helper.clazz.equals(byte.class) || helper.clazz.equals(Byte.class)) {

                field.set(result, bf.get());
            } else if (helper.clazz.equals(boolean.class) || helper.clazz.equals(Boolean.class)) {
                field.set(result, bf.get() != 0 ? true : false);
            } else if (helper.clazz.equals(short.class) || helper.clazz.equals(Short.class)) {
                field.set(result, (short) ((bf.get() << 8) | (bf.get() & 0xff)));
            } else if (helper.clazz.equals(char.class) || helper.clazz.equals(Character.class)) {

            } else if (helper.clazz.equals(int.class) || helper.clazz.equals(Integer.class)) {


                field.set(result, (int) ((bf.get() << 24) | (bf.get() << 16 | (bf.get() << 8 | (bf.get())))));
            } else if (helper.clazz.equals(float.class) || helper.clazz.equals(Float.class)) {

                field.set(result, Float.intBitsToFloat((int) ((bf.get() << 24) | (bf.get() << 16 | (bf.get() << 8 | (bf.get()))))));

            } else if (helper.clazz.equals(long.class) || helper.clazz.equals(Long.class)) {
                field.set(result, (long) ((bf.get() << 56) | (bf.get() << 48) | (bf.get() << 40) | (bf.get() << 32) | (bf.get() << 24) | (bf.get() << 16 | (bf.get() << 8 | (bf.get())))));
            } else if (helper.clazz.equals(double.class) || helper.clazz.equals(Double.class)) {

                field.set(result, Double.longBitsToDouble((long) ((bf.get() << 56) | (bf.get() << 48) | (bf.get() << 40) | (bf.get() << 32) | (bf.get() << 24) | (bf.get() << 16 | (bf.get() << 8 | (bf.get()))))));

            } else if (helper.clazz.equals(String.class)) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte tempValue = 3;
                while ((tempValue = bf.get()) != 3) {
                    bos.write(tempValue);
                }
                field.set(result, new String(bos.toByteArray()));
            }

        }

        return result;
    }

    private static final class Helper {
        public String name;
        public Class<?> clazz;

        @Override
        public String toString() {
            return "Helper{" +
                    "name='" + name + '\'' +
                    ", clazz=" + clazz +
                    '}';
        }
    }


}

