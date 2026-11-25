# GradeTrack - Student Grade and GPA Tracking System (Java Swing)

OOP101 Final Project. Pure Java (JDK 11+), no external libraries, no database.

## Run
- Easiest: double-click `GradeTrack.jar`, or `java -jar GradeTrack.jar`
- From source: `run.bat` (Windows) or `./run.sh` (Mac/Linux) - compiles to `out/` then starts the app.
- NetBeans / IntelliJ / Eclipse: new project from existing sources, source folder = `src`, main class = `gradetrack.Main`.

Data is saved automatically to `<your home folder>/.gradetrack/gradetrack_data.txt`
(pass another path as the first argument to use a different file).

## Tabs (matches the documentation)
| Tab | What it does |
|---|---|
| Overview | Every course with its current grade, progress bar and % graded |
| Courses | New course (credits + weights totalling 100%), Edit weights, category scores, add / update / delete / search / sort scores, "Other..." custom types, delete course |
| Target grade | Average still needed on the remaining work to reach a target |
| GPA | Credit-weighted GPA (1.00 highest) with each course's credits and percent |

## Classes and the OOP concepts they show
| Package.Class | Responsibility | OOP / Java concepts |
|---|---|---|
| model.Course | Name, credits, category weights, list of scores | classes/objects, constructors (overloaded + `this(...)` chaining), `this`, encapsulation, access modifiers, static members |
| model.Assessment (abstract) | Name, score, total of one graded item | abstraction, encapsulation, abstract method `getType()` |
| model.Quiz / Test / Examination / Laboratory / Other | The four built-in score types, plus `Other` for types the student types in ("Other...") | inheritance, polymorphism, method overriding (`getType`, `describe`) |
| logic.GradeCalculator | Current grade, % graded, category score | methods with parameters/return values, **method overloading** (`toPercent`), static + instance members |
| logic.TargetGradePlanner | Average needed to reach a target | return values, input validation, `throws` |
| logic.GPACalculator | Credit-weighted GPA, percent -> grade point | static members, collections |
| logic.CourseManager | Add/delete courses; add/update/delete/search/sort scores | ArrayList, HashMap, Comparator sorting, linear search, enum |
| logic.InputValidator | Parses and validates typed input | try-catch, multi-catch, custom exceptions |
| exception.* | InvalidInputException -> InvalidScoreException, InvalidWeightException, DuplicateCourseException, DuplicateTypeException | custom exceptions, inheritance, `throw` / `throws` |
| storage.StorageManager | Saves/loads all data to a text file | file reading/writing, `finally`, try-with-resources |
| ui.* (MainFrame + 4 tabs) | Swing interface | instance members, interface (`Refreshable`), event listeners |

## How grades are computed
- Each course starts with four types (Quiz, Test, Examination, Laboratory); "Other..." adds more. Weights always add up to 100%.
- Category score = points earned / points graded in that category.
- Current grade = weighted average of the categories that have graded work. Ungraded work (blank score) never changes it.
- % graded = sum of weight x (graded points / all points) over the categories.
- Target average = (target - weighted points already earned) / (weight remaining) x 100.
- GPA scale (1.00 best): 97-100 = 1.00, 94-96 = 1.25, 91-93 = 1.50, 88-90 = 1.75, 85-87 = 2.00, 82-84 = 2.25, 79-81 = 2.50, 76-78 = 2.75, 75 = 3.00, below 75 = 5.00.
  This reproduces the documentation example (Physics 77.7 / Calculus 85.8 / Intro 88.0, 4+3+3 credits -> GPA 2.23).
  Change `GPACalculator.toGradePoint` if your school uses another scale.

## Tests
`test/gradetrack/GradeTrackTests.java` is a plain-Java check (60 checks) of the documented examples, validation, search/sort and storage.
```
javac -d out $(find src -name '*.java')
javac -cp out -d testout test/gradetrack/GradeTrackTests.java
java -cp out:testout gradetrack.GradeTrackTests
```

## Put it online (GitHub Pages + CheerpJ)
The `web/` folder is the website: `index.html` + `GradeTrack.jar`. CheerpJ (a free Java-in-the-browser runtime) runs the same Swing app, and saves data in the visitor's browser.
1. Create a GitHub repository (for example `GradeTrackjava`) and upload the two files inside `web/` (not the folder itself) to the repository root.
2. Repository **Settings -> Pages -> Build and deployment**: Source = *Deploy from a branch*, Branch = `main`, folder = `/ (root)`, then Save.
3. After a minute the site is live at `https://<your-username>.github.io/<repository-name>/`.
Test locally first (the page must be served over http, not opened as a file): `cd web && python3 -m http.server 8080`, then open http://localhost:8080 (needs internet to load CheerpJ).
After changing the Java code, rebuild the jar with `javac --release 17` and replace `web/GradeTrack.jar`.
