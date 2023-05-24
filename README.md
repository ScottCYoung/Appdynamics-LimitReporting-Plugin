# AppDynamics Limit Reporting Plugin

This plugin, when installed, will begin reporting node level limits being reached as events in the appdynamics controller.

## Requirements
- Agent version 21.+
- Java 8


## Deployment steps
- Copy AgentPlugin-#.#.jar file under <agent-install-dir>/ver.x.x.x.x/sdk-plugins

## **How does it work**
Using the Java iSDK, the plugin listens for for events that indicate that an agent metric limit has been reached.  These metric limits are part of standard configurations and can be adjusted based on environment differences across application stacks depending on scale and functionality of the applications themselves.  
  
When the plugin sees one of these limits reached, it will correlate the event to the Application and automatically send an event to the controller using the built in AppDynamics Agent event publisher, creating a WARNING event with the Message "AGENT_METRIC_REG_LIMIT_REACHED".

Logging is also done so invocation of the plugin can be seen in the logs.
