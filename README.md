Realm Relay
===========

A relay server, written in Java, designed for constructing and modifying Realm
of the Mad God packets. Currently compatible with game client v27.3.1.

General Information
-------------------
- Hacks for Realm Relay are written in JavaScript.
- A new instance of each script is created for each relay user.
- All information is lost when the user disconnects from the relay.
- Some variable names are unusual, because they are obfuscated in the client.
- Variable names and data object are subject to change in later game version.
- Not all game packets are supported by the relay and are not implemented.
- `$` may be used universally as an alias to call functions from.

Events
------
- All event handlers have one or more parameters.
- The first parameter is always the event object.
- There are two types of events: `ScheduledScriptEvent` and `PacketScriptEvent`
- The event object is necessary to perform many tasks.


##### onEnable(ScheduledScriptEvent)
Fires when a user connects to the relay.

##### onConnect(ScheduledScriptEvent)
Fires once the relay has successfully established a connection to the remote host.

##### onConnectFail(ScheduledScriptEvent)
Fires if the relay fails to connect to the remote host.

##### onDisconnect(ScheduledScriptEvent)
Fires when the relay disconnects from the remote host.

##### onClientPacket(PacketScriptEvent)
Fires when the relay receives a packet from the client.

##### onServerPacket(PacketScriptEvent)
Fires when the relay receives a packet from the server.
