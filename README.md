# EnchantedCharactersView
A single line text view. When new text is set, old characters that can be reused shifts to the new position.

<img src="https://landicefu.github.io/EnchantedCharactersView/pic/demo.gif" width=300/>

## Latest Version
V1.0 has been released.
- You can now use interpolators to make the animation more vivid.

## Usage
```xml
    <tw.lifehackers.widget.EnchantedCharactersView
            android:id="@+id/enchantedCharactersView"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            app:fadeInForNonMovingChar="true"
            app:typeface="Adlanta.otf"
            app:text="January"
            app:textColor="@android:color/black"
            app:textSize="48sp"
            app:gravity="center_horizontal"/>
```

| Attribute | Type      | Description          |
| --------- | ----------|--------------------- |
| text      | string    | Default text to show |
| textColor | color     | The color of the text|
| textSize  | dimension | The size of the text |
| typeface  | string    | filepath to the typeface file under asset folder |
| fadeInForNonMovingChar| boolean | Make the characters fade in when it's not available from last text |
| animationSteps | int | Number of steps between start and end. (The larger the longer one animation takes)|
| gravity | enum | The gravity of the text |
