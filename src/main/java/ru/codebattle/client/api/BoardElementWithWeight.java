package ru.codebattle.client.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BoardElementWithWeight {
    NONE(' ', 0, 0),         // пустое место
    WALL('☼', -100, -100),         // а это стенка
    START_FLOOR('#', -100, -100),  // место старта змей
    OTHER('?', -100, -100),        // этого ты никогда не увидишь :)

    APPLE('○', 1, 1),        // яблоки надо кушать от них становишься длинее
    STONE('●', -3, 2),        // а это кушать не стоит - от этого укорачиваешься
    FLYING_PILL('©', 1, 1),  // таблетка полета - дает суперсилы
    FURY_PILL('®', 5, 1),    // таблетка ярости - дает суперсилы
    GOLD('$', 2, 2),         // золото - просто очки

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
    TAIL_END_DOWN('╙', -5, -5),
    TAIL_END_LEFT('╘', -5, -5),
    TAIL_END_UP('╓',-5, -5),
    TAIL_END_RIGHT('╕', -5, -5),
    TAIL_INACTIVE('~', -5, -5),

    // туловище твоей змейки
    BODY_HORIZONTAL('═', -5, -5),
    BODY_VERTICAL('║', -5, -5),
    BODY_LEFT_DOWN('╗', -5, -5),
    BODY_LEFT_UP('╝', -5, -5),
    BODY_RIGHT_DOWN('╔', -5, -5),
    BODY_RIGHT_UP('╚', -5, -5),

    // змейки противников
    ENEMY_HEAD_DOWN('˅', -100, 2),
    ENEMY_HEAD_LEFT('<', -100, 2),
    ENEMY_HEAD_RIGHT('>', -100, 2),
    ENEMY_HEAD_UP('˄', -100, 2),
    ENEMY_HEAD_DEAD('☺', 0, 0),   // этот раунд противник проиграл
    ENEMY_HEAD_EVIL('♣', -100, 0),   // противник скушал таблетку ярости
    ENEMY_HEAD_FLY('♦', 0, 0),    // противник скушал таблетку полета
    ENEMY_HEAD_SLEEP('ø', -100, -100),  // змейка противника ожидает начала раунда

    // хвосты змеек противников
    ENEMY_TAIL_END_DOWN('¤', 0, 0),
    ENEMY_TAIL_END_LEFT('×', 0, 0),
    ENEMY_TAIL_END_UP('æ', 0, 0),
    ENEMY_TAIL_END_RIGHT('ö', 0, 0),
    ENEMY_TAIL_INACTIVE('*', 0, 0),

    // туловище змеек противников
    ENEMY_BODY_HORIZONTAL('─', -100, 2),
    ENEMY_BODY_VERTICAL('│', -100, 2),
    ENEMY_BODY_LEFT_DOWN('┐', -100, 2),
    ENEMY_BODY_LEFT_UP('┘', -100, 2),
    ENEMY_BODY_RIGHT_DOWN('┌', -100, 2),
    ENEMY_BODY_RIGHT_UP('└', -100, 2);

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
