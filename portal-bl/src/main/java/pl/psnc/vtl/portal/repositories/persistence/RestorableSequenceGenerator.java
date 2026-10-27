package pl.psnc.vtl.portal.repositories.persistence;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * A sequence generator that doesn't generate a new id when the object is given
 * an id smaller than ever generated. This makes it possible to restore
 * previously deleted entities for undo/redo purposes.
 */
@Slf4j
public class RestorableSequenceGenerator extends SequenceStyleGenerator {
    String getterMethod;
    Long lastGenerated;

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object)
            throws HibernateException {
        try {
            Method method = object.getClass().getMethod(getterMethod);
            Long givenId = (Long) method.invoke(object);
            if (givenId != null) {
                if (lastGenerated == null)
                    lastGenerated = (Long) super.generate(session, object);
                if (givenId <= lastGenerated)
                    return givenId;
            }
        } catch (Exception e) {
            log.error("Checking given id failed for class {}", object.getClass(), e);
        }
        lastGenerated = (Long) super.generate(session, object);
        return lastGenerated;
    }

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        super.configure(type, params, serviceRegistry);

        String targetColumn = params.getProperty("target_column");
        getterMethod = "get" + targetColumn.substring(0, 1).toUpperCase() + targetColumn.substring(1);
    }
}
