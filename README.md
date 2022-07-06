# Tip Calculator
A simple tip calculator made with Kotlin.

## Features
- Computes for tips given a base amount
  - Adjustable tip percentage (0-30%)
  - A custom tip can also be set!
- Splitting a bill between `n` people
- Toggleable currency from PHP to USD (display only)
- Saving and displaying of tip computation history using RecyclerView

## Demo
### Calculator
This feature allows you to calculate for tips given a base bill amount. The tip percentage can be adjusted through the slider, and if a specific tip amount is prefered, the wrench button can be clicked to bring up a custom tip input. The bill can be split using the split-between feature that allows you to input a number `n` which is then used to divide the bill. Lastly, the currency used by the UI can be switched between PHP or USD.

![](https://media.giphy.com/media/3gVIcHPrzUbL3rGfUE/giphy.gif)

### Saving & History
This feature allows you to save tip computations and view your history. A saved bill's name can be set throug the prompt upon clicking on the save menu item. Once saved, you will be redirected to the bill history page, displaying your list of saved bills. Here, details about the name, tip, total, and split (if applicable) are shown.

![](https://media.giphy.com/media/Y2AqURQNR08rUj3iCJ/giphy.gif)
