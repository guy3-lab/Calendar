### Instructions:
Supported Commands:

//creates a single event from the specified starting time to the specified ending time:

create event `<eventSubject>` from `<dateStringTtimeString>` to `<dateStringTtimeString>`
- `<eventSubject>` is a string  
- `<dateStringTtimeString>` = `"YYYY-MM-DDThh:mm"`

//creates an event that starts at some time and ends at some time that repeats throughout the provided weekdays for `<N>` amount of times 

create event `<eventSubject>` from `<dateStringTtimeString>` to `<dateStringTtimeString>` repeats `<weekdays>` for `<N>` times
- `<weekdays>` = values without separation e.g. `MT` Valid values: `M,T,W,R,F,S,U`  
- `<N>` = number of repetitions


//creates an event that starts at some time and ends at some time that repeats throughout the provided weekdays until the specified date

create event `<eventSubject>` from `<dateStringTtimeString>` to `<dateStringTtimeString>` repeats `<weekdays>` until `<dateString>`

//creates an all day event that has the starting time at 8 AM to 5 PM

create event `<eventSubject>` on `<dateString>`

//creates an all day event series that repeats throughout the provided weekdays for `<N>` amount of times

create event `<eventSubject>` on `<dateString>` repeats `<weekdays>` for `<N>` times

//creates an all day event series that repeats throughout the provided weekdays until the specified date

create event `<eventSubject>` on `<dateString>` repeats `<weekdays>` until `<dateString>`

//edits an event's property to a new value depending on the event's subject, start time, and end time

edit event `<property>` `<eventSubject>` from `<dateStringTtimeString>` to `<dateStringTtimeString>` with `<NewPropertyValue>`
- `<property>`: `subject`, `start`, `end`, `description`, `location`, `status`  
- `<NewPropertyValue>`: value to set (string or `YYYY-MM-DDThh:mm`)

//edits an event's property to a new value beginning from the specified start time within the series

edit events `<property>` `<eventSubject>` from `<dateStringTtimeString>` with `<NewPropertyValue>`

//edits the entire series' property to the provided new value by identifying the start time

edit series `<property>` `<eventSubject>` from `<dateStringTtimeString>` with `<NewPropertyValue>`

//prints events on the specified day

print events on `<dateString>`

//prints events with the specified time constraints

print events from `<dateStringTtimeString>` to `<dateStringTtimeString>`

//shows whether a time of day has an event going on. If there is, prints "busy", if not, prints "available"

show status on `<dateStringTtimeString>`


//create a new calendar with a unique name and timezone as specified by the user.

create calendar --name `<calName>` --timezone `<area/location>`
- `<calName>`: The name of the calendar
- `<area/location>`: The timezone in the area/location format.

//used to change/modify an existing property ( name or timezone ) of the calendar. The command is invalid if the property being changed is absent or the value is invalid in the context of the property.

edit calendar --name `<name-of-calendar>` --property `<property-name>` `<new-property-value>`

//set the calendar context

use calendar --name `<name-of-calendar>`

//copy a specific event with the given name and start date/time from the current calendar to the target calendar to start at the specified date/time.

copy event `<eventName>` on `<dateStringTtimeString>` --target `<calendarName>` to `<dateStringTtimeString>`

//same behavior as the copy event above, except it copies all events scheduled on that day. The times remain the same, except they are converted to the timezone of the target calendar.

copy events on `<dateString>` --target `<calendarName>` to `<dateString>`

//copies all events scheduled in the specified date interval.

copy events between `<dateString>` and `<dateString>` --target `<calendarName>` to `<dateString>`

For all `copy events` commands, if an event series partly overlaps with the specified range, only those events in the series that overlap with the specified range should be copied
---
## How to Use

1. Choose either **interactive** or **headless** mode to run the program. `--mode interactive` or `--mode headless <command_file>`
3. For **headless mode**, provide a text file with a list of valid commands (the last command must be `exit`).  
4. For **interactive mode**, the user types valid commands one-by-one and sees output immediately.

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
- ✔ Create an event series where a single event spans multiple days
- ✔ Can create multiple calendars with their own individual events
- ✔ Can change the name and time zones of calendars
- ✔ Can copy events from one calendar to the other, and coverting the times as needed

---

## Features That Don't Work

- ✘ Cannot create two of the same events
- ✘ Cannot create two series of the same name or starting time
- ✘ Cannot specify `status` or `description` or `location` at creation time  

---

## Rough Distribution of Work

**Krish**  
- Controller
- Model
- View
- Command Line Interface & Files  
- Tests  

**David**  
- Model  
- Tests  
- Instructions

## Changes from the last assignment
- Added a check condition when making and editing series such that no two series can have the same starting time
- Added a seriesKey field in the Event's class to do what was listed above
- Made AddEventHelper method in the initial calendar class protected so that an extending class can have acces to it when creating and adding events
- Added the new class SpecificCalendar that extends Calendar because it now has a name and timezone field in order to conform to open for extension, closed for modification (that is if Calendar was already good)
- Added a new package MultiCalendar in the model in order to represent the multiple calendars that can be accessed in Assignment 5.

