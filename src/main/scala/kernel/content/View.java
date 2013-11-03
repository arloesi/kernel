package kernel.content;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({TYPE,METHOD,FIELD})
@Retention(RUNTIME)
public @interface View {
  Class<?> target() default DEFAULT.class;
  static final class DEFAULT {};
}