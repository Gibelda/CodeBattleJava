package ru.codebattle.client.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BoardElementWithWeight {
    NONE(' ', 0, 0),         // пустое место
    WALL('☼', -1000, -1000),         // а это стенка
    START_FLOOR('#', -1000, -1000),  // место старта змей
    OTHER('?', -1000, -1000),        // этого ты никогда не увидишь :)

    APPLE('○', 30, 30),        // яблоки надо кушать от них становишься длинее
    STONE('●', -5, 10),        // а это кушать не стоит - от этого укорачиваешься
    FLYING_PILL('©', 0, 0),  // таблетка полета - дает суперсилы
    FURY_PILL('®', 30, 30),    // таблетка ярости - дает суперсилы
    GOLD('$', 30, 30),         // золото - просто очки

    // голова твоей змеи в разных состояниях и напрвлениях
    HEAD_DOWN('▼', 0, 0),
    HEAD_LEFT('◄', 0, 0),
    HEAD_RIGHT('►', 0, 0),
    HEAD_UP('▲', 0, 0),
    HEAD_DEAD('☻', 0, 0),    // этот раунд ты проиграл
    HEAD_EVIL('♥', 0, 0),    // ты скушал таблетку ярости
    HEAD_FLY('♠', 0, 0),     // ты скушал таблетку полета
    HEAD_SLEEP('&', 0, 0),   // твоя змейка ожидает начала раунда

    // хвост твоей змейки
    TAIL_END_DOWN('╙', 0, 0),
    TAIL_END_LEFT('╘', 0, 0),
    TAIL_END_UP('╓',0, 0),
    TAIL_END_RIGHT('╕', 0, 0),
    TAIL_INACTIVE('~', 0, 0),

    // туловище твоей змейки
    BODY_HORIZONTAL('═', -10, -10),
    BODY_VERTICAL('║', -10, -10),
    BODY_LEFT_DOWN('╗', -10, -10),
    BODY_LEFT_UP('╝', -10, -10),
    BODY_RIGHT_DOWN('╔', -10, -10),
    BODY_RIGHT_UP('╚', -10, -10),

    // змейки противников
    ENEMY_HEAD_DOWN('˅', -1000, 30),
    ENEMY_HEAD_LEFT('<', -1000, 30),
    ENEMY_HEAD_RIGHT('>', -1000, 30),
    ENEMY_HEAD_UP('˄', -1000, 30),
    ENEMY_HEAD_DEAD('☺', -1000, -1000),   // этот раунд противник проиграл
    ENEMY_HEAD_EVIL('♣', -1000, 20),   // противник скушал таблетку ярости
    ENEMY_HEAD_FLY('♦', 0, 0),    // противник скушал таблетку полета
    ENEMY_HEAD_SLEEP('ø', -1000, -1000),  // змейка противника ожидает начала раунда

    // хвосты змеек противников
    ENEMY_TAIL_END_DOWN('¤', -5, 0),
    ENEMY_TAIL_END_LEFT('×', -5, 0),
    ENEMY_TAIL_END_UP('æ', -5, 0),
    ENEMY_TAIL_END_RIGHT('ö', -5, 0),
    ENEMY_TAIL_INACTIVE('*', -5, 0),

    // туловище змеек противников
    ENEMY_BODY_HORIZONTAL('─', -1000, 30),
    ENEMY_BODY_VERTICAL('│', -1000, 30),
    ENEMY_BODY_LEFT_DOWN('┐', -1000, 30),
    ENEMY_BODY_LEFT_UP('┘', -1000, 30),
    ENEMY_BODY_RIGHT_DOWN('┌', -1000, 30),
    ENEMY_BODY_RIGHT_UP('└', -1000, 30);

    final char symbol;
    final int weight;
    final int evilWeight;

    @Override
    public String toString() {
        return String.valueOf(symbol);
    }

    public static BoardElementWithWeight valueOf(char ch) {
        for (BoardElementWithWeight el : BoardElementWithWeight.values()) {
            if (el.symbol == ch) {
                return el;
            }
        }
        throw new IllegalArgumentException("No such element for " + ch);
    }
}
