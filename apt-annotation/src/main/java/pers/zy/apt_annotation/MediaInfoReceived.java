package pers.zy.apt_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * date: 4/19/21   time: 12:01 PM
 * author zy
 * Have a nice day :)
 **/

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface MediaInfoReceived {
}
