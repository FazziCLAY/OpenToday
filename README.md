# OpenToday
Android application for the organization of life

[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/fazziclay/opentoday?style=plastic)](https://github.com/FazziCLAY/OpenToday/releases)
[![IzzyOnDroid](https://img.shields.io/endpoint?style=plastic&url=https://apt.izzysoft.de/fdroid/api/v1/shield/ru.fazziclay.opentoday)](https://apt.izzysoft.de/fdroid/index/apk/ru.fazziclay.opentoday)

# Using
Your main friends are Items
There are different types of items that you can add:
* **Text** - use it for simple text notes
* **CheckBox** - in connection with GroupItem, use it as a grocery list to the store or a to-do list for today
* **Group** - there are no restrictions in depth! Create your own hierarchy of storing items
* **Filter Group** - use it to indicate current birthdays, tasks for today, various schedules (for example, school) or something more, up to the second
and others... (very useful thing)


## Items tree
```css
Item - (minimal heigth, backgroud color)
|
| Text - (text, text color)
  |
  | LongText - (long text, long text color)
  | Group - (items)
  | FilterGroup - (items)
  | CycleList - (items)
  | Counter - (current value, step)
  | Checkbox - (is checked)
     |
     | DayRepeatableCheckbox - (start value for 'is checked' in Checkbox, latest regenerate date)
   ```

# Screenshots
|![1](https://user-images.githubusercontent.com/68351787/199270739-5e7491ed-f345-4347-ac8a-a6160090414e.jpg) | ![1](https://user-images.githubusercontent.com/68351787/199270753-53d74768-63e6-4564-a889-e2025ed78d19.jpg) | ![About app](https://user-images.githubusercontent.com/68351787/199270769-080177ea-5368-485a-aa23-3a75e87a0695.jpg) | ![Calendar](https://user-images.githubusercontent.com/68351787/199270761-d21b86d9-9059-4578-ae0a-f3aacb73e1c9.jpg)
|:---:|:---:|:----:|:---:|
|![1](https://user-images.githubusercontent.com/68351787/199270781-a832ca3e-0da1-4480-b1ff-9134c9c41751.jpg) | ![1](https://user-images.githubusercontent.com/68351787/199270788-c29d92ab-b585-440b-90b1-2e2c9bb001b5.jpg)


# Explore app!

# For developers
"/docs" folder

## Todo/Ideas:
* [ ] ItemsStorage for notification quick note
* [ ] Items transform (Text -> Group; Checkbox -> CheckboxDayRepeatable and etc...)
* [ ] Fix notifications in TickSession
* [ ] Toolbar -> Selection -> SelectALL & DeselectALL
* [ ] Settings -> minimize paddings (left, right, bottom, top)
* [ ] Replace checkboxItem to text item & add 'modules' to item and add Module 'checkbox'

Make a pull request -> you will be added to contributors.json and also I will create the contributors screen in the application

## 

