package kernel.service;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD})
public @interface Remote {
  public String name() default "";
  public String perm() default "";
}
