package com.fazziclay.opentoday.util.time;

/**
 * Режим конфвертации времени специально для {@link TimeUtil#convertToHumanTime}
 * <p>HHMMSS - 00:00:00. Обратите внимание, час показывается независимо от значения 0</p>
 * <p>hhMMSS - 01:00:00 & 00:00. Теперь час показывается только если > 0</p>
 * <p>HHMM - 24:59. Теперь секунды будут отрезаны</p>
 *
 * @see TimeUtil#convertToHumanTime(int, ConvertMode)
 **/
public enum ConvertMode {
    HHMMSS,
    hhMMSS,
    HHMM
}
