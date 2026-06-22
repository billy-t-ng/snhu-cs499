# Enhancement Three: Databases

[Back to Home](index.md)

## Description

This is the same weight tracking Android app from CS 360 used in the previous two enhancements. By this point, the app had working filters, a trend summary, sorting and search, and a functional goal system. What still needed attention was the database itself.

## Justification

The database had a few problems that would not hold up in a real application. The biggest issue was that passwords were stored as plain text. If someone got access to the database file, they could read every password directly.

The `onUpgrade` method was also a problem. Any time the database schema changed, it dropped every table and rebuilt the database from scratch, wiping out the stored application data in the process. The weights table also had no index, so every filtered query had to go through the entire table row by row. The goal weight was still saved in `SharedPreferences` instead of the database, which meant every account on the device shared the same goal.

Passwords are now hashed with SHA-256 before being stored. When a user creates an account, the password runs through Java's `MessageDigest`, gets converted to a hex string, and that value is saved instead of the plain-text password. When the user logs in, the same process runs on what they type in, and the two hashes are compared.

A goals table was added to replace the `SharedPreferences` setup from Enhancement One. Each user now has their own goal stored in the database and tied to their username. Setting a new goal updates the existing goal record for that user. The dashboard was updated to read and write goals through the database, and the add entry screen now reads the database-backed goal when checking whether to send an SMS alert.

An index was added to the weights table on the username and date columns. This gives the database a faster way to look up entries when the date filters from Enhancement Two are used, instead of scanning every row.

The `onUpgrade` method was rewritten so it no longer drops every table and wipes out the stored application data. Instead of deleting everything, it checks the current database version and only applies the changes that are needed. When the database is upgraded from the old structure to the new one, the app creates the goals table, adds the weights index, and rebuilds the users table so new accounts store hashed passwords instead of plain-text passwords. Existing weight entries stay intact through the process.

## Course Outcomes

CO5 was the main focus of this enhancement. Storing passwords as plain text is one of the most common security mistakes an app can make, and hashing them is an important step toward protecting user data.

CO3 came into play through the migration logic, which required figuring out what could be kept and what had to be rebuilt during the upgrade.

CO4 was addressed through the indexing and the decision to move goal storage into SQLite where it is properly tied to each user.

## Reflection

The most important part of this enhancement was thinking through how database changes affect existing data. The original `onUpgrade` method handled schema changes by dropping every table and rebuilding the database, which was simple but unsafe because it erased stored data. Rewriting that forced me to think more carefully about what needed to change and what needed to be preserved.

The final upgrade approach keeps existing weight entries intact while adding the new goals table, index, and updated user table structure. The tradeoff is that existing accounts from the older version may need to be recreated, but the weight entry data is preserved.

Moving the goal weight from `SharedPreferences` into SQLite also fixed a real design issue. The earlier approach worked for a prototype, but it did not properly support multiple users because every account on the same device shared the same goal value. Adding a goals table made the feature more consistent with the rest of the app because each goal is now tied to a specific username.

The password update was the most important security improvement. The original app stored passwords as plain text, which meant anyone with access to the database could read them directly. Hashing passwords before storing them removes that weakness and makes the data layer safer.

The index on the weights table was a smaller change, but it tied back to the date filtering from Enhancement Two by making username-and-date queries more efficient as the amount of stored data grows.

## Links

* [Before Code](https://github.com/billy-t-ng/snhu-cs499/tree/master)
* [Enhanced Code](https://github.com/billy-t-ng/snhu-cs499/tree/enhancement-3-databases)
* [Download Enhancement Three Narrative](narratives/enhancement-three-narrative.docx)

