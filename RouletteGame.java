import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class RouletteGame {

    private Integer roundTime;
    private String serverStatus;
    private Integer serverCounter;

    private Thread playerBet;
    private Thread serverCounte;

    private Integer currentNumber;

    private String[] players;
    private List<PlayerBets> playerBets = new ArrayList<>();

    private BufferedReader br;

    private void StartGame() {
        try {

            roundTime = readFromFileInteger("config.txt", 3, 0);
            serverStatus = readFromFileString("config.txt", 1, 0);
            serverCounter = readFromFileInteger("config.txt", 2, 0);
            players = readCompleteFileString("players.txt").split(",");

            serverStatus.trim();
            if (serverStatus.equals("Stopped")) {
                serverStatus = "Running";
                System.out.println("Server is Up and Running");
                writeToFile("config.txt", serverStatus, 1, 0);
            } else {

                if (serverCounter > roundTime) {
                    System.out.println("Server is already Up and Running \nNext Round start in "
                            + ((roundTime + 10) - serverCounter));
                } else {
                    System.out.println(
                            "Server is already Up and Running \nCurrect Round end in " + (roundTime - serverCounter));

                }

            }

            System.out.println("Connected Players are : ");
            for (int i = 1; i <= players.length; i++) {
                System.out.println(i + ") " + players[i - 1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void writeToFile(String filePath, String data, int line, int position) throws IOException {
        RandomAccessFile file = new RandomAccessFile(filePath, "rw");

        Integer i = 1;
        while (i < line) {
            file.readLine();
            i++;
        }

        file.write(data.getBytes());
        file.close();
    }

    private static void writeToFile(String filePath, String data) throws IOException {
        RandomAccessFile file = new RandomAccessFile(filePath, "rw");
        file.skipBytes((int) file.length());
        file.writeUTF(data);
        file.write(0x0a);
        file.close();
    }

    private static String readFromFileString(String filePath, int line, int position) throws IOException {
        String text;
        RandomAccessFile file = new RandomAccessFile(filePath, "r");

        Integer i = 1;
        while (i < line) {
            file.readLine();
            i++;
        }

        text = file.readLine();
        file.close();

        return text;
    }

    private static String readCompleteFileString(String filePath) throws IOException {
        RandomAccessFile file = new RandomAccessFile(filePath, "r");
        StringBuffer buffer = new StringBuffer();

        Boolean isNotFirst = false;
        while (file.getFilePointer() < file.length()) {
            if (isNotFirst) {
                buffer.append(",");
            }
            isNotFirst = true;
            buffer.append(file.readLine());
        }
        String contents = buffer.toString();
        file.close();

        return contents;
    }

    private static Integer readFromFileInteger(String filePath, int line, int position) throws IOException {

        Integer value = 0;

        RandomAccessFile file = new RandomAccessFile(filePath, "r");

        Integer i = 1;
        while (i < line) {
            file.readLine();
            i++;
        }

        value = Integer.parseInt(file.readLine());
        file.close();

        return value;
    }

    private void startCounter() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                serverCounter++;
                if (serverCounter == (roundTime - 10)) {
                    System.out.println("Round will end in 10 Sec!");
                } else if (serverCounter == roundTime) {
                    System.out.println("Round is Over!");
                    System.out.println("New Round will start in 10 Sec!");

                    currentNumber = generateNumber();

                    System.out.println("All User Bets result : ");

                    if (playerBets.size() > 0) {
                        for (int i = 0; i < playerBets.size(); i++) {
                            if (playerBets.get(i).getType().equalsIgnoreCase("EVEN")) {
                                if (currentNumber % 2 == 0) {
                                    playerBets.get(i).setStatus("WIN");
                                    playerBets.get(i)
                                            .setPrice((Integer.parseInt(playerBets.get(i).getAmmount()) * 2) + "");
                                } else {
                                    playerBets.get(i).setStatus("LOSS");
                                    playerBets.get(i).setPrice("0");
                                }
                            } else if (playerBets.get(i).getType().equalsIgnoreCase("ODD")) {
                                if (currentNumber % 2 != 0) {
                                    playerBets.get(i).setStatus("WIN");
                                    playerBets.get(i)
                                            .setPrice((Integer.parseInt(playerBets.get(i).getAmmount()) * 2) + "");
                                } else {
                                    playerBets.get(i).setStatus("LOSS");
                                    playerBets.get(i).setPrice("0");
                                }
                            } else {
                                if (currentNumber == Integer.parseInt(playerBets.get(i).getAmmount())) {
                                    playerBets.get(i).setStatus("WIN");
                                    playerBets.get(i)
                                            .setPrice((Integer.parseInt(playerBets.get(i).getAmmount()) * 36) + "");
                                } else {
                                    playerBets.get(i).setStatus("LOSS");
                                    playerBets.get(i).setPrice("0");
                                }
                            }
                        }

                        for (int i = 0; i < playerBets.size(); i++) {
                            System.out.println(playerBets.get(i).toString());
                        }

                        for (int i = 0; i < playerBets.size(); i++) {
                            try {
                                writeToFile("history.txt", playerBets.get(i).toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        System.out.println("NO USER BETS FOUNDS!");
                    }

                    System.out.println("Number is : " + currentNumber);
                    System.out.println();
                } else if (serverCounter == (roundTime + 10)) {
                    System.out.println("New Round is started!");
                    System.out.println();
                    System.out.println("Make Bet : <Player Name> <BET Type> <Ammount>");
                    serverCounter = 0;

                    playerBets = new ArrayList<>();
                }
                try {
                    writeToFile("config.txt", serverCounter.toString().length() == 1 ? "0" + serverCounter.toString()
                            : serverCounter.toString(), 2, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, new Date(), 1000);
    }

    private void StartPlayersBet() {

        String input;
        br = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.println();
            System.out.println("Make Bet : <Player Name> <BET Type> <Ammount>");
            input = br.readLine();

            if (input.length() > 0) {
                if (input.split(" ").length == 3) {
                    String[] inputs = input.split(" ");
                    if (Arrays.asList(players).contains(inputs[0])) {
                        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
                        if (inputs[1].equals("EVEN") || inputs[1].equals("ODD") || (pattern.matcher(inputs[1]).matches()
                                && (Integer.parseInt(inputs[1]) >= 0 && Integer.parseInt(inputs[1]) <= 36))) {
                            if (pattern.matcher(inputs[2]).matches()) {
                                PlayerBets bet = new PlayerBets(inputs[0], inputs[1], inputs[2]);
                                playerBets.add(bet);
                            } else {
                                System.out.println("Please provide currect Ammount!");
                            }
                        } else {
                            System.out.println("Please provide valid Type eg. EVEN, ODD and Number beetween 0 to 36!");
                        }
                    } else {
                        System.out.println("Player is not connected with given details!");
                    }

                } else {
                    System.out.println("Please make currect input!");
                }
            } else {
                System.out.println("Please make currect input!");
            }

            StartPlayersBet();

        } catch (IOException e) {
            if (serverCounter < roundTime) {
                StartPlayersBet();
            } else {
                System.out.println("Please wait to Start new round!");
            }
        }
    }

    private Integer generateNumber() {
        Random number = new Random();
        Integer randomNumber = number.nextInt(36);

        return randomNumber;
    }

    public static void main(String[] args) {
        RouletteGame game = new RouletteGame();
        game.StartGame();

        game.playerBet = new Thread(new Runnable() {
            public void run() {
                game.StartPlayersBet();
            }
        });

        game.serverCounte = new Thread(new Runnable() {
            public void run() {
                game.startCounter();
            }
        });

        game.playerBet.start();
        game.serverCounte.start();
    }
}