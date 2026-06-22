# Enhancement Two: Algorithms and Data Structures

[Back to Home](index.md)

## Description

This is the same weight tracking Android app from CS 360 that was enhanced in the previous milestone. At this point, the app had a working goal system, separated adapter classes, and a fully navigable SMS screen. What it still lacked was a meaningful way to organize, analyze, and present the data users were putting into it.

## Justification

Although the app was good at collecting weight entries, it did not do much with that data after it was saved. The filter chips on the dashboard all called the same method and returned the same results no matter which one was selected. The history screen only showed entries for a single date through a calendar. There was no trend tracking, no search, no sorting, and no way to step back and see the bigger picture.

The dashboard chips now perform real date-range filtering. When a user taps `7D`, `30D`, or `90D`, the app calculates a cutoff date and queries the database for only the entries that fall within that range. This replaced the old setup where every chip returned the same results.

A trend summary was also added alongside the filtering. After the filtered entries load, the app looks at the oldest and newest weights in that range and shows whether the user is trending up, down, or flat, and by how much. If the selected range only has one entry or none, the app tells the user there is not enough data for a trend instead of trying to calculate one.

The history screen now offers two ways to browse the data. Toggle chips at the top let the user switch between the original calendar view and a new list view. The list view shows all entries at once with a search bar and a sort toggle. Typing in the search bar filters the list in real time by date or note. Tapping the sort label flips between newest-first and oldest-first. Entries on the same date are also sorted by their database ID so the order stays consistent.

The calendar view also received a small improvement. It now shows a count of how many entries exist for the selected date, so the user can quickly tell whether that day has data.

## Course Outcomes

CO3 was addressed through the filtering and trend logic. Both required working through how dates are stored, how to compare them as text in SQLite, and how to handle cases like empty ranges or ranges with only one entry.

CO4 was addressed through the decisions around data flow on the history screen. The app loads entries into memory, filters and sorts them in Java, and updates the adapter with the results instead of querying the database on every keystroke.

## Reflection

One part that required careful attention was how dates are stored and compared. The date filtering relies on comparing strings in the database with a `>=` operator, which only works if every date follows the same format. The app already used `YYYY-MM-DD`, so the comparison worked without needing to change the stored date format. If even one entry had been saved in a different format, the filter could have returned incorrect results without an obvious error.

One issue that came up was with the chip listeners. The `ChipGroup` was not responding to taps even though everything looked correct on screen. After debugging, the issue turned out to be a missing style attribute on each chip in the XML. Without the choice chip style applied, the chips looked right visually but the `ChipGroup` did not reliably register them as selected. Adding the correct style resolved the issue.

The list view on the history screen came together more smoothly. The app loads the entries into an in-memory list, filters them based on the search query, sorts the results, and then passes the updated list to the adapter. This made real-time search straightforward because the app only needs to reapply the filter whenever the text changes. At this scale, keeping the data in memory is a reasonable approach because the dataset is expected to remain small.

## Links

* [Before Code](artifacts/enhancement-one/)
* [Enhanced Code](artifacts/enhancement-two/)
* [Download Enhancement Two Narrative](narratives/enhancement-two-narrative.docx)

