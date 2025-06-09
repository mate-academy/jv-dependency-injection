package mate.academy.lib;

import java.lang.annotation.Annotation;

public class ComponentImpl implements Component {
    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}
