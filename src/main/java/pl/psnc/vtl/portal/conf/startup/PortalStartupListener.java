package pl.psnc.vtl.portal.conf.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class PortalStartupListener {


    @EventListener(classes = { ContextRefreshedEvent.class })
    public void onApplicationRefreshedEvent(ContextRefreshedEvent event) {
    }

}
