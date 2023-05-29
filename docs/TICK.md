# OpenToday/Tick
(Translated by translate.google.com)
TL;DR
Tick, Tick-rate, Tick behavior.

The tick system is one of the fundamental concepts in OpenToday.
In fact, tic can be compared with the heartbeat and blood circulation in animals. Every second the application
starts a tick and it goes deep into everything that can be ticked. (Tabs, aitems, notifications of aitems).

The tick ensures the operability of notifications, resetting the state of the 'Daily tick', the work of the 'Group with a Filter (FilterGroup)'

Consider a small example.
The user has 2 tabs, the first tab has 'Daily tick #1' and 'Text #2'.
In the second, the user has a 'Group with Filter #3', inside which there is a 'Daily tick #4' and 'Text #5' (with notification)

Now consider a single tick. I will remind you every second the whole process under consideration will be repeated. It is important.
It is also important that for notifications to work in the background, the application plans a "Personal tick" in the AlarmManager (sm. "Personal tick")
So the tick goes through the tabs, the first tab will tick first, it in turn will tick the 'Daily tick #1' which in
turn will reset if the current day is not equal to the day of the last reset.
Next, the 'Text #2' will tick, the text does not have a tick functionality, but the parent of the Text, namely the Aitem (the parent class of all aitems)
has a tick functionality for ticking notifications. But even here there is a failure, there are no notifications for Text #2.

Now it's time to tick the second tab
It will tick the 'Group with Filter #3', which in turn, depending on the "Tick Behavior" configured in it, will tick the items inside.
Let's say the tick behavior is set to: ACTIVE (tick only active ones), and also let's assume that all filters give a green
signal, that is, both items inside are active.
So first tick 'Daily tick #4', and IT's IMPORTANT! the daily tick will tick the part of the code responsible for updating
the tick even if this item would be inactive in the Group with the Filter, but the tick notifications will be ticked only if
the item was active.
And then the 'Text #5' will tick, which will tick notifications inside, and notifications will be sent to the user according to the situation.

## Personal tick
There are times when it is not necessary to tick all the items, let's say we know that aitma N has a notification at 11:00
and we want to schedule a tick of a specific item via Android AlarmManager.
There is a nuance here if we tick this particular item, then if it is inside the FilterGroup, the filters will be completely
ignored, so a kind of personal tick "Tick on the way" is used for notifications.
This tick restricts the allowed items for the tick only to those that are on the way to item N.
Accordingly, if the filters along the way give a red signal, then the personal tick will not reach its addressee,
and if the signal is green, then the notification will come instantly.