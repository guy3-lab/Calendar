a. Instructions
Supported Commands:
//creates a single event from the specified starting time to the specified ending time
create event <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString>
- <eventSubject> is a string
- <dateStringTtimeString>
	- <dateString> is a string in the form "YYYY-MM-DD"
	- <timeString> is a string in the form "hh:mm"
	- combined: "YYYY-MM-DDThh:mm"

//creates an event that starts at some time and ends at some time that repeats throughout the provided weekdays for <N> amount of times 
create event <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> for <N> times
- <weekdays> is a sequence of characters in a string that denote days of the week (e.g. 'M', 'T', 'W', 'R', 'F', 'S', 'U') separated by commas
- <N> is a number that specify how many times to repeat the event on specific weekdays

//creates an event that starts at some time and ends at some time that repeats throughout the provided weekdays until the specified date
create event <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> until <dateString>

//creates an all day event that has the starting time at 8 AM to 5 PM
create event <eventSubject> on <dateString>

//creates an all day event series that repeats throughout the provided weekdays for <N> amount of times
create event <eventSubject> on <dateString> repeats <weekdays> for <N> times

//creates an all day event series that repeats throughout the provided weekdays until the specified date
create event <eventSubject> on <dateString> repeats <weekdays> until <dateString>

//edits an event's property to a new value depending on the event's subject, start time, and end time
edit event <property> <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString> with <NewPropertyValue>
- <property> may be one of the following: subject start , end , description , location , status . The format of the new property values are string , dateStringTtimeString , dateStringTtimeString , string , string and string respectively
- <NewPropertyValue> is a string of the value the user wants to change the property to

//edits an event's property to a new value beginning from the specified start time within the series
edit events <property> <eventSubject> from <dateStringTtimeString> with <NewPropertyValue>

//edits the entire series' property to the provided new value by identifying the start time
edit series <property> <eventSubject> from <dateStringTtimeString> with <NewPropertyValue>

//prints events on the specified day
print events on <dateString>

//prints events with the specified time constraints
print events from <dateStringTtimeString> to <dateStringTtimeString>

//shows whether a time of day has an event going on. If there is, prints "busy", if not, prints "available"
show status on <dateStringTtimeString>

How to Use:
1. Choose either of the two modes to run -- interactive or headless
2. If headless mode, provide a text file with a list of the valid commands. Ensure that the last command is an exit command
3. If interactive, user is able type the valid commands one by one and see the results immediately

b. Which features work and which do not.
That work:
- Can create an event on a specified date
- Can retrieve all events on that specified date
- Can create a series starting at a specified date
- When creating a series, it will correctly create the corresponding events within the calendar
- Can retrieve all events that are specified between a specific start time and end time
- Can show if a day is available during a specific time or busy
- Supports user ability to edit an event's subject, start time, end time, description, location, and status

Doesn't work:
- Does not support retrieving all the events in a series
- Does not support creating two of the same events
- Does not support creating an event series in which the singular event spans over one day
- Does not support allowing the user to specify the event, status, and description when creating the event

c. Rough Distribution of Work:
Krish: 
-Controller
-View
-Command Line Interface and Files
-Tests

David:
-Model
-Controller
-Tests
-Instructions