package com.cisco.josouthe;

import com.appdynamics.agent.api.AppdynamicsAgent;
import com.appdynamics.agent.api.Transaction;
import com.appdynamics.agent.api.impl.NoOpTransaction;
import com.appdynamics.instrumentation.sdk.Rule;
import com.appdynamics.instrumentation.sdk.SDKClassMatchType;
import com.appdynamics.instrumentation.sdk.SDKStringMatchType;
import com.appdynamics.instrumentation.sdk.template.AGenericInterceptor;
import com.appdynamics.instrumentation.sdk.toolbox.reflection.IReflector;
import com.appdynamics.instrumentation.sdk.toolbox.reflection.ReflectorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LimitInformerInterceptor extends AGenericInterceptor {

    IReflector getName;

    IReflector getCorrelationID, get;

    public LimitInformerInterceptor() {
        getName = getNewReflectionBuilder().invokeInstanceMethod("getName", true).build(); //String
        getCorrelationID = getNewReflectionBuilder().accessFieldValue( "correlationID", true ).build(); //CorrelationId
        get = getNewReflectionBuilder().invokeInstanceMethod("get", true, new String[]{ String.class.getCanonicalName() }).build();
    }

    @Override
    public Object onMethodBegin(Object objectIntercepted, String className, String methodName, Object[] params) {

        switch (methodName) {
            case "throwCappedAgentEvent": {
                if( params.length < 6 ) return null;
/*  public void throwCappedAgentEvent(EventType type, NotificationSeverity severity, String message, Map<String, String> details, EntityDefinition entityDefinition, String subType) {
        AgentEventData agentEventData = new AgentEventData(ClockUtils.getCurrentTime(), severity, type, details, message);
        agentEventData.setTriggeredEntity(entityDefinition);
        agentEventData.setSubType(subType);
        this.eventGenerationService.addApplicationEventIgnoreLimits(agentEventData);
        this.lastGeneratedEvents.put(String.valueOf(type), new ADLong(ClockUtils.getCurrentTime()));
    }
*/
                AppdynamicsAgent.getEventPublisher().publishEvent(String.valueOf(params[2]), "WARN", "AGENT_METRIC_REG_LIMIT_REACHED", (Map<String,String>)params[3]);
                break;
            }
            case "throwCappedInternalEvent": {
/*
    public void throwCappedInternalEvent(EventType type, NotificationSeverity severity, String message, Map<String, String> details, Map<String, Object> properties) {
        AgentEventData agentEventData = new AgentEventData(ClockUtils.getCurrentTime(), severity, type, details, message);
        agentEventData.setEventProperties(properties);
        this.eventGenerationService.addApplicationEventIgnoreLimits(agentEventData);
        this.lastGeneratedEvents.put(String.valueOf(type), new ADLong(ClockUtils.getCurrentTime()));
    }

    public void throwCappedInternalEvent(String eventTypeKey, String message, Map<String, String> details) {
        this.eventGenerationService.addApplicationEventIgnoreLimits(new AgentEventData(ClockUtils.getCurrentTime(), NotificationSeverity.WARN, EventType.AGENT_EVENT, details, message));
        this.lastGeneratedEvents.put(eventTypeKey, new ADLong(ClockUtils.getCurrentTime()));
    }
 */
                String message = "UNKNOWN";
                Map<String,String> details = null;
                if( params.length == 5) {
                    message = (String) params[2];
                    details = (Map<String, String>) params[3];
                } else if( params.length == 3 ) {
                    message = (String) params[1];
                    details = (Map<String, String>) params[2];
                } else {
                    //oops
                }
                AppdynamicsAgent.getEventPublisher().publishEvent(message, "WARN", "AGENT_METRIC_REG_LIMIT_REACHED", details);

                break;
            }
        }

        return null;
    }

    @Override
    public void onMethodEnd(Object state, Object object, String className, String methodName, Object[] params, Throwable exception, Object returnVal) {

    }

    @Override
    public List<Rule> initializeRules() {
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule.Builder(
                "com.singularity.ee.agent.commonservices.eventgeneration.events.InternalEventGenerator")
                .classMatchType(SDKClassMatchType.INHERITS_FROM_CLASS)
                .methodMatchString("throwCappedAgentEvent")
                .methodStringMatchType(SDKStringMatchType.EQUALS)
                .build()
        );
        rules.add(new Rule.Builder(
                "com.singularity.ee.agent.commonservices.eventgeneration.events.InternalEventGenerator")
                .classMatchType(SDKClassMatchType.INHERITS_FROM_CLASS)
                .methodMatchString("throwCappedInternalEvent")
                .methodStringMatchType(SDKStringMatchType.EQUALS)
                .build()
        );
        return rules;
    }

    public String getCorrelationHeader( Object message ) {
        if( message == null ) return null;
        try {
            return (String) get.execute(message.getClass().getClassLoader(), message, new Object[]{ AppdynamicsAgent.TRANSACTION_CORRELATION_HEADER});
        } catch (ReflectorException e) {
            getLogger().info("Exception trying to read appd header from message naive attempt, exception: "+ e.getMessage());
        }
        return null;
    }

    private Object paramsToString(Object[] param) {
        if( param == null || param.length == 0 ) return "";
        StringBuilder sb = new StringBuilder();
        for( int i =0 ; i< param.length; i++ ) {
            if( param[i] == null ) {
                sb.append("notSure null");
            } else {
                sb.append(param[i].getClass().getCanonicalName());
                sb.append(" ").append(String.valueOf(param[i]));
            }
            if( i < param.length-1 ) sb.append(", ");
        }
        return sb.toString();
    }
}
