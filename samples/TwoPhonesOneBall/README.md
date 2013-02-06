![logo](http://update.orbotix.com/developer/sphero-small.png)

# Two Phones One Ball

Two phones one ball is a sample demonstrating how to control a Sphero from a phone that is not directly connected to the ball, but is controlling it through an intermediate.  The two phones are communicating through a Wifi Router and one phone is sending drive and LED commands to the other phone that are then relayed to a Sphero.

## Multiplayer

For future reference,  we are planning to deprecate our multiplayer code.  We decided to make this decision, because there are better 3rd party mobile real-time multiplayer services out there, like [http://playphone.com/](http://playphone.com/).

**Our current multiplayer service has the following limitations:**

1. Does not work on 3G or 4G connections
2. Requires the router be set up with peer to peer communications allowed
3. Requires the router allow multicast broadcasts

We found that many business routers did not satisfy items 2 and 3, and hence our push towards another multiplayer service.  

However, you may still use this sample to build multiplayer games.

## Questions

For questions, please visit our developer's forum at [http://forum.gosphero.com/](http://forum.gosphero.com/)

