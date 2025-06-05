### Instructions:
Supported Commands:

//creates a single event from the specified starting time to the specified ending time
create event <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString>
- `<eventSubject>` is a string  
- `<dateStringTtimeString>` = `"YYYY-MM-DDThh:mm"`

//creates an event that starts at some time and ends at some time that repeats throughout the provided weekdays for <N> amount of times 
create event <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> for <N> times
- `<weekdays>` = comma-separated values like `M,T,W,R,F,S,U`  
- `<N>` = number of repetitions


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
- `<property>`: `subject`, `start`, `end`, `description`, `location`, `status`  
- `<NewPropertyValue>`: value to set (string or `YYYY-MM-DDThh:mm`)

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


---

## How to Use

1. Choose either **interactive** or **headless** mode to run the program.  
2. For **headless mode**, provide a text file with a list of valid commands (the last command must be `exit`).  
3. For **interactive mode**, the user types valid commands one-by-one and sees output immediately.

---

## Features That Work

- ✔ Can create an event on a specified date  
- ✔ Can retrieve all events on that specified date  
- ✔ Can create a repeating event series  
- ✔ Event series correctly populates on the calendar  
- ✔ Can retrieve events between two specific times  
- ✔ Can check if a time is "busy" or "available"  
- ✔ Can edit an event’s:
  - Subject  
  - Start time  
  - End time  
  - Description  
  - Location  
  - Status  

---

## Features That Don't Work

- ✘ Cannot retrieve *all events in a series* as a group  
- ✘ Cannot create two of the same events  
- ✘ Cannot create an event series where a single event spans multiple days  
- ✘ Cannot specify `status` or `description` at creation time  

---

## Rough Distribution of Work

**Krish**  
- Controller  
- View  
- Command Line Interface & Files  
- Tests  

**David**  
- Model  
- Controller  
- Tests  
- Instructions  

