# Enhancement One: Software Design and Engineering

[Back to Home](index.md)

## Description

The artifact is a weight tracking Android app originally built in CS 360: Mobile Architecture and Programming. It is written in Java and uses SQLite to store data locally on the device. Users can create an account, log in, record their weight over time, view recent entries on a dashboard, use a history screen with a calendar, and access an SMS alert feature.

## Justification

I chose this artifact because it had a solid foundation, but several features were unfinished or only partially built. The enhancement focused on cleaning up the structure, finishing incomplete features, and improving overall usability.

The `WeightAdapter` and `DayAdapter` classes were pulled out of `MainActivity` and `HistoryActivity` into their own files. In the original version, these were private static inner classes inside the activity files. Since each adapter has its own interface, view holder, and logic, keeping them buried inside larger activity files made the project harder to read and navigate. Moving them into separate files made the code cleaner and easier to maintain.

The goal weight feature was also built out from a placeholder into a working feature. The original app displayed `To goal: --` on the dashboard with no way to set a goal. The SMS trigger also used a hardcoded goal of `150.0` and a hardcoded phone number. Those hardcoded values were replaced with user input. Users can now tap the goal section on the dashboard to set their own target weight, and the dashboard shows how far they are from reaching it. SMS alerts only trigger when the user has set a goal, entered a phone number, and turned alerts on.

The SMS permission screen was improved from a dead end into a fully navigable screen. It now has a back arrow, bottom navigation matching the rest of the app, a phone number input field, and a toggle to turn alerts on or off.

The floating action button was also removed from the navigation bar on all screens. Instead, users can tap the current weight section on the dashboard to add a new entry, which feels more natural for this app.

## Course Outcomes

This enhancement met the course outcomes planned in Module One. CO1 was addressed through code restructuring and improved user flow. CO2 was addressed through the code review and written explanation of the enhancement. CO4 was addressed through the design decisions made throughout the project, including separating concerns, replacing hardcoded values, and improving navigation across the app.

## Reflection

This enhancement showed me that a project can have a strong foundation and still feel unfinished. Many of the main pieces were already there, but several features needed more work before the app felt complete. It was like a house where the first floor is built but the second floor is still just framing. The structure was there, but it was not ready to live in yet.

One decision that came up was how to store the goal weight. A full goals table was planned for the database enhancement later, so I used `SharedPreferences` as a temporary solution for this stage. That kept Enhancement One focused on software design without jumping ahead into the database work.

The SMS permission screen ended up being more involved than expected. Adding the back button was straightforward, but adding the bottom navigation created a layout issue. The original screen used padding on the root layout, which worked before the navigation bar was added. Once the navigation bar was anchored to the bottom, that padding pushed it away from the edge. Fixing the issue meant moving the padding off the root layout and onto individual elements as margins instead.

Overall, this enhancement reinforced the value of going back into existing code and asking what it would take to make it feel finished, not just functional.

## Links

* [Original Code](artifacts/original/)
* [Enhanced Code](artifacts/enhancement-one/)
* [Download Enhancement One Narrative](narratives/enhancement-one-narrative.docx)

