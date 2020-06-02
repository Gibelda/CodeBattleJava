package ru.codebattle.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

import ru.codebattle.client.api.*;

public class Main {

    private static final String SERVER_ADDRESS = "http://codebattle-pro-2020s1.westeurope.cloudapp.azure.com/codenjoy-contest/board/player/iu6fs8g2wv2laakcppdz?code=3055726951586902261&gameName=snakebattle";
    public static void main(String[] args) throws URISyntaxException, IOException {
        SnakeBattleClient client = new SnakeBattleClient(SERVER_ADDRESS);
        client.run(gameBoard -> {
            long date = new Date().getTime();
            Direction direction = Strategy.chooseRightDirection(gameBoard);
            boolean act = Strategy.chooseAct(gameBoard);
            System.out.println("Time: " + (new Date().getTime() - date) + " milliseconds");
            return new SnakeAction(act, direction);
        });
        System.in.read();

        client.initiateExit();
    }
}
