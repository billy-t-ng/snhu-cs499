---
layout: single
title: CS 499 ePortfolio
permalink: /
---

# CS 499 ePortfolio

# CS 499 ePortfolio

## Professional Self-Assessment

The Computer Science program covered a lot of ground. Over the course of the degree, I worked on mobile apps, full-stack web applications, databases, AI concepts, security, and team-based software development. No single course covered everything, but looking back, each one added something that showed up again later.

The capstone brought a lot of those pieces together. Taking a project from an earlier course and improving it across three different areas felt different from building something brand new. It was not just about learning another tool or getting something to run. It was about going back to old code, seeing what was unfinished or weak, and figuring out how to make it better.

Even if my career path does not stay in traditional software development, the skills from this program still carry over. Being able to look at a system, understand how it works, spot what needs to change, and explain that clearly is useful in any technical field.

## Collaborating in a Team Environment

Working in a team came up in several courses throughout the program. CS-250 introduced Agile and Scrum, which was my first real look at how software teams are organized and how different roles contribute to the same project. That course made it clear that being a good team member is not just about writing code. It is also about understanding what other people are working on and how your part fits into the bigger picture.

CS-465 reinforced that idea by splitting a full-stack application into frontend and backend parts that had to work together. In the capstone, the code review served a similar purpose. Even though the work was individual, I still had to present the code in a way that someone else could follow. Explaining what I changed, why I changed it, and what still needed work is part of collaboration too.

## Communicating with Stakeholders

Writing the narratives for each enhancement ended up being more useful than I expected. Explaining what changed is the easy part. Explaining why it changed, what the tradeoffs were, and how the change improved the project takes more thought.

The code review video was similar. I had to walk through the project in a way that made sense to someone who had not been staring at the code for hours. That meant focusing less on every small implementation detail and more on the reasoning behind the work. That kind of communication matters in real jobs too, especially when explaining technical decisions to a manager, client, or team member who may not work directly in the code.

## Data Structures and Algorithms

In CS-300, data structures and algorithms were mostly assignments. In the capstone, they became tools I needed to make the app more useful. The original weight tracking app stored entries, but it did not do much with them after that. The Algorithms and Data Structures enhancement changed that by adding date-range filtering, a trend summary, sorting, and real-time search.

Each feature needed a slightly different approach. The date filters were handled through database queries because the app only needed entries inside a selected range. Sorting used a comparator, with the database ID as a secondary value so entries from the same day stayed in a consistent order. Search was handled with a TextWatcher that re-filters the list as the user types. At this scale, keeping the list in memory made sense because the dataset is small enough to manage without adding unnecessary complexity.

## Software Engineering and Databases

The program covered software engineering and databases through courses like CS-360, CS-465, and CS-340. In the capstone, the Software Design and Engineering enhancement focused on cleaning up the app structure. I pulled adapter classes out of the activity files, finished parts of the app that were still placeholders, and cleaned up navigation so the screens felt more connected.

The Database enhancement focused on the data layer. The original app stored passwords in plain text, kept the goal weight in SharedPreferences, had no index for the weight queries, and used an onUpgrade method that dropped the tables when the schema changed. Those were fine for an early class project, but they were not good long-term design choices.

The database work fixed those issues by hashing passwords before storage, adding a goals table, creating an index on the weights table, and rewriting the upgrade logic so existing weight entries are not destroyed during a database change. Working on the same app across multiple enhancements showed how connected the layers are. The goal feature from the first enhancement became stronger once the goal was moved into the database. The date filters from the second enhancement made more sense once the username and date columns were indexed.

## Security

Security showed up most clearly in the Database enhancement, but it was something the program had been building toward for a while. CS-405 covered secure coding practices, and that helped change how I approached the app. In earlier projects, I usually focused on making sure the feature worked. I did not always think as much about what could go wrong if someone got access to the data. Seeing plain-text passwords sitting in the database made it obvious how easily a weakness like that could be taken advantage of.

With the database enhancement, I had to think beyond whether the login feature worked for the user. I also had to think about what would happen if the data was exposed or handled carelessly. The password hashing, migration logic, upgrade handling, and decisions about what to preserve or rebuild all came back to the same question: what happens if this is not done carefully? Since the original table stored passwords as plain text, one option would have been to read each existing password, hash it, and save the hashed version back. For this project, I chose the simpler approach of rebuilding the users table so new accounts use hashed passwords going forward while preserving the existing weight entry data. In a real application, that would need to be handled more carefully, most likely through a proper migration or a forced password reset.

## How the Artifacts Fit Together

The ePortfolio is built around one Android application enhanced across three categories. I think that tells a better story than using three unrelated projects. Each enhancement builds on the last one. The first enhancement cleaned up the structure and made the app easier to work with. The second enhancement made better use of the weight data through filtering, sorting, search, and trends. The third enhancement improved the database so the app handled user data in a safer and more organized way.

What started as a course project is now closer to a real application. It has cleaner code, more useful data features, and a stronger data layer. The sections that follow include the code review, the original and enhanced code, and the narratives for each enhancement.

## Portfolio Navigation

* [Code Review](code-review.md)
* [Enhancement One: Software Design and Engineering](enhancement-one.md)
* [Enhancement Two: Algorithms and Data Structures](enhancement-two.md)
* [Enhancement Three: Databases](enhancement-three.md)
* [Artifacts](artifacts.md)
