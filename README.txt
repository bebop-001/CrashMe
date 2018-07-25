Sun Jul 22 17:32:32 PDT 2018

keywords: android logcat Thread.UncaughtExceptionHandler
keywords: Thread.setDefaultUncaughtExceptionHandler
keywords: android.permission.WRITE_EXTERNAL_STORAGE
keywords: checkSelfPermission onRequestPermissionsResult

I have an app that sometimes dies several hours after being
started if its on the backstack and was having a hard time
catching the crash dump.  I could run logcat on the device
using a terminal and logcat as a process on the app but
as it turns out, output from logcat is extremely limited
unless you have a root'ed device.

I needed something to catch the stack trace from the
exception for later review and this is a something I have
needed in the past to.  My solution is the CrashLog
class in this demo.

I also like putting things under one directory and being
an old unix guy, I like the idea of a home directory.
Also I wanted to be able to put the home directory out
in common memory so for instance an email app could
access the crash logs as attachements.  This is something
else I've written many variations of over time.  The
HomeDirectory class in this demo does that for me.

Last, I needed something as a test bed and decided to 
do it in such a way others with the same problems could
use it.  So CrashMe is the demo.  It has a button
that causes a divide by zero exception, a button
to show the most recent crash, and a button to clear
the crash logs.

Hope someone finds this useful.

Steve Smith
