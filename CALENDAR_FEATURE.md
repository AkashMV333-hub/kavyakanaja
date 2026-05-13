# Calendar Feature Implementation

## What's New

The app now includes an **interactive calendar** that allows users to explore poems from any date in the past or future.

## Features

### 1. **Date Selection**
   - Users can tap the **"📅 Pick Date"** button on the Home screen
   - A Material3 DatePicker dialog opens
   - Select any date (past or future)
   - The poem for that date is instantly displayed

### 2. **Automatic Reset to Today**
   - When the app is reopened or restarted, the calendar **automatically defaults to today's date**
   - This is achieved by initializing the selected date to `getTodayMillis()` on every app launch
   - The selected date is NOT persisted (fresh start each session)

### 3. **Poem Cycling**
   - The `poemOfTheDay()` function uses a deterministic formula:
     ```kotlin
     dayIndex = (epochDays % poems.size).toInt()
     ```
   - As users navigate through dates, poems cycle through the collection
   - Once all poems have been shown (after `poems.size` days), the cycle **repeats from the start**
   - This ensures continuous rotation without running out of poems

### 4. **Date Display**
   - The header shows: **"Poem for [Date]"** (e.g., "Poem for May 13, 2026")
   - The date format is locale-aware and readable

## How It Works

### Modified File: `HomeScreen.kt`

**New State Variables:**
```kotlin
val todayMillis = getTodayMillis()                      // Today at midnight (UTC)
val selectedDateMillis = remember { mutableStateOf(todayMillis) }  // Selected date state
val showDatePicker = remember { mutableStateOf(false) } // Show/hide date picker
val datePickerState = rememberDatePickerState(initialSelectedDateMillis = todayMillis)
```

**UI Components:**
- A `Row` with the date title and "Pick Date" button
- A `DatePickerDialog` that opens when the button is tapped
- The selected date is passed to `poemOfTheDay()`:
  ```kotlin
  val poem = poemOfTheDay(poems.value, selectedDateMillis.value)
  ```

**Helper Functions:**
- `getTodayMillis()`: Returns today's date at midnight in milliseconds
- `formatDateForDisplay(millis)`: Converts milliseconds to a readable date string

## User Flow

1. **First Launch**: App shows poem for today's date
2. **Tap "📅 Pick Date"**: DatePickerDialog appears
3. **Select a Date**: User picks any date from the calendar
4. **Tap "OK"**: Dialog closes, new poem displays
5. **Scroll Up/Down**: View the poem's text, meanings, and options
6. **Close and Reopen App**: Calendar resets to today's date

## Example Scenarios

| Scenario | Result |
|----------|--------|
| User picks May 1, 2026 | Shows poem at index `(daysSinceEpoch % poemsCount)` |
| User picks May 2, 2026 | Shows poem at next index in cycle |
| User picks a date 2 years in future | Cycles through poems but keeps rotating |
| App is closed and reopened | Returns to today's date automatically |
| After `N` days (where `N = poems.size`) | Poems repeat from the beginning |

## Technical Details

### Poem Determinism
The same date always produces the same poem because:
- Each date converts to a unique "epoch day" number
- `epochDay % poems.size` gives a consistent index
- This index always maps to the same poem

### No Persistence
By design, the **selected date is not saved to DataStore or SharedPreferences**:
- Ensures fresh state on every app launch
- Prevents confusion if the user manually changes system time
- Aligns with user expectation: "I'll open the app and see today's poem"

### Why This Works With Cycling
If your poems list has 50 poems:
- Day 0 → Poem 0
- Day 1 → Poem 1
- ...
- Day 49 → Poem 49
- Day 50 → Poem 0 (cycles back)
- Day 500 → Poem 50 % 50 = Poem 0
- Day 501 → Poem 1
- And so on...

## Future Enhancements (Optional)

1. **Save Favorite Dates**: Store frequently-visited dates in Preferences
2. **Poetry Collections by Poet**: Filter calendar by poet (e.g., "Show only Basaveshvara poems")
3. **Poem Streaks**: Track consecutive days the user has read a poem
4. **Highlights on Calendar**: Mark dates where they've marked a poem as favorite
5. **Swipe Navigation**: Navigate between dates by swiping left/right

## Files Changed

- `app/src/main/java/com/example/kavyakanaja/ui/screens/HomeScreen.kt` (modified)
  - Added DatePreference UI
  - Integrated DatePickerDialog from Material3
  - Added helper functions for date handling
  - Updated Poem-of-the-Day selection to use selected date

## Testing the Feature

1. **Run the app locally:**
   ```powershell
   .\gradlew.bat assembleDebug
   ```

2. **In the emulator/device:**
   - Open the app
   - Verify the header shows today's date
   - Tap "📅 Pick Date"
   - Select different dates
   - Observe poems change deterministically
   - Close and reopen the app
   - Confirm the calendar resets to today

3. **Build Status:**
   - ✅ No Kotlin compile errors
   - ✅ All imports correct
   - ✅ Calendar feature ready to test

