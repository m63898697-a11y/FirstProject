import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        System.setOut(new java.io.PrintStream(
                new java.io.FileOutputStream(java.io.FileDescriptor.out), true, "UTF-8"));

        // ВАЖНО: только ASCII-символы — у них гарантированная ширина 1.
        // Эмодзи в разных терминалах имеют разную ширину (1 или 2),
        // из-за чего правая граница сетки "съезжает".
        String person = "@";  // игрок
        String castle = "C";  // замок

        Scanner scanner = new Scanner(System.in);

        System.out.println("Привет! Давай поиграем? Если что герой-@, замок-C, Самые слабые монстры 1 уровня-g, более сильные монстры 2 уровня-m, самые сильные монстры 3 уровня-w (Напиши: ДА или НЕТ)");
        String answer = scanner.nextLine().trim();
        System.out.println("Ваш ответ: " + answer);

        if (!answer.equals("ДА")) {
            System.out.println("Жаль, что ты не хочешь играть.");
            return;
        }

        System.out.println("Выбери сложность игры (от 1 до 5):");
        if (!scanner.hasNextInt()) {
            System.out.println("Некорректный ввод.");
            return;
        }
        int difficultGame = scanner.nextInt();
        scanner.nextLine(); // съедаем оставшийся \n после nextInt()

        if (difficultGame < 1) difficultGame = 1;
        if (difficultGame > 5) difficultGame = 5;

        System.out.println("Выбранная сложность: " + difficultGame);

        int sizeBoard;
        int maxMonsters;
        int personLive;

        switch (difficultGame) {
            case 1:  sizeBoard = 5;  maxMonsters = 19;  personLive = 5; break;
            case 2:  sizeBoard = 8;  maxMonsters = 78;  personLive = 4; break;
            case 3:  sizeBoard = 10; maxMonsters = 94;  personLive = 3; break;
            case 4:  sizeBoard = 15; maxMonsters = 219; personLive = 2; break;
            default: sizeBoard = 20; maxMonsters = 394; personLive = 1; break;
        }

        int personX = 1;
        int personY = sizeBoard;
        int step = 0;

        Random random = new Random();

        int castleX = sizeBoard;
        int castleY = 1;

        String[][] board = new String[sizeBoard][sizeBoard];
        for (int y = 0; y < sizeBoard; y++)
            for (int x = 0; x < sizeBoard; x++)
                board[y][x] = " ";

        int availableCells = (sizeBoard * (sizeBoard - 1)) - 1;
        int countMonster = (int) (maxMonsters * Math.random()) + 1;
        if (countMonster > availableCells) countMonster = availableCells;

        while (countMonster > 0) {
            int mx = random.nextInt(sizeBoard);
            int my = random.nextInt(sizeBoard - 1);
            if (mx == castleX - 1 && my == castleY - 1) continue;
            if (!board[my][mx].equals(" ")) continue;
            board[my][mx] = getMonsterSymbol(random.nextInt(3));
            countMonster--;
        }

        board[castleY - 1][castleX - 1] = castle;

        while (true) {
            board[personY - 1][personX - 1] = person;
            outputBoardWithGrid(board, personLive);

            System.out.println("Куда будешь ходить? (ход возможен только по вертикали или горизонтали на одну клетку)");
            System.out.println("Координаты персонажа — (x: " + personX + ", y: " + personY + ")");
            System.out.print("Введи x y: ");

            // Аккуратно читаем ДВА целых числа
            if (!scanner.hasNextInt()) {
                System.out.println("Некорректные координаты. Попробуй снова.");
                scanner.nextLine();
                continue;
            }
            int x = scanner.nextInt();

            if (!scanner.hasNextInt()) {
                System.out.println("Некорректные координаты. Попробуй снова.");
                scanner.nextLine();
                continue;
            }

            int y = scanner.nextInt();

            System.out.println("Ты выбрал: " + x + ", " + y);

            boolean isValidMove = (x >= 1 && x <= sizeBoard && y >= 1 && y <= sizeBoard);
            boolean isAdjacent = (Math.abs(x - personX) == 1 && y == personY)
                    || (Math.abs(y - personY) == 1 && x == personX);

            if (!isValidMove) {
                System.out.println("Координаты вне поля. Попробуй снова.");
                continue;
            } else if (!isAdjacent) {
                System.out.println("Можно ходить только на соседнюю клетку по вертикали или горизонтали. Попробуй снова.");
                continue;
            }

            String targetCell = board[y - 1][x - 1];
            if (targetCell.equals(" ")) {
                board[personY - 1][personX - 1] = " ";
                personX = x;
                personY = y;
                step++;
                System.out.println("Ход корректный; новые координаты: " + personX + ", " + personY);
                System.out.println("Номер хода: " + step);
            } else if (targetCell.equals(castle)) {
                System.out.println("Ты победил!");
                break;
            } else {
                System.out.println("На пути монстр! Реши задачу, чтобы победить.");
                if (taskMonster(difficultGame, scanner)) {
                    board[personY - 1][personX - 1] = " ";
                    personX = x;
                    personY = y;
                    step++;
                    System.out.println("Монстр побеждён! Ты прошёл дальше.");
                } else {
                    personLive--;
                    System.out.println("Монстр победил! Жизни уменьшены. Осталось: " + personLive);
                    if (personLive <= 0) {
                        System.out.println("У тебя не осталось жизней. Игра окончена.");
                        break;
                    }
                }
            }
        }
    }

    private static void outputBoardWithGrid(String[][] board, int personLive) {
        int n = board.length;
        int cellWidth = 5;

        printHorizontalLine(n, cellWidth);

        for (int y = 0; y < n; y++) {
            System.out.print("|");
            for (int x = 0; x < n; x++) {
                String cell = centerInBlock(board[y][x], cellWidth);
                System.out.print(cell + "|");
            }
            System.out.println();

            if (y < n - 1) {
                printHorizontalLine(n, cellWidth);
            }
        }

        printHorizontalLine(n, cellWidth);
        System.out.println("Жизни игрока: " + personLive);
        System.out.println();
    }

    private static void printHorizontalLine(int n, int cellWidth) {
        System.out.print("+");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < cellWidth; j++) {
                System.out.print("-");
            }
            System.out.print("+");
        }
        System.out.println();
    }

    private static int visualWidth(String s) {
        int w = 0;
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            if (cp == 0xFE0F || cp == 0xFE0E) {
                // селектор стиля — ширины не даёт
            } else if (cp >= 0x10000) {
                w += 2;
            } else {
                w += 1;
            }
            i += Character.charCount(cp);
        }
        return w;
    }

    private static String centerInBlock(String content, int width) {
        if (content == null || content.isEmpty()) content = " ";
        int w = visualWidth(content);
        int totalPadding = Math.max(0, width - w);
        int leftPadding = totalPadding / 2;
        int rightPadding = totalPadding - leftPadding;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leftPadding; i++) sb.append(' ');
        sb.append(content);
        for (int i = 0; i < rightPadding; i++) sb.append(' ');
        return sb.toString();
    }

    // Все символы — ASCII ширины 1
    private static String getMonsterSymbol(int type) {
        switch (type) {
            case 0:  return "g";  // ghost
            case 1:  return "m";  // mage
            default: return "w";  // wolf
        }
    }

    private static boolean taskMonster(int difficulty, Scanner scanner) {
        Random random = new Random();
        String question;
        int expected;

        if (difficulty <= 2) {
            int a = random.nextInt(20) + 1;
            int b = random.nextInt(20) + 1;
            boolean add = random.nextBoolean();
            if (add) {
                question = a + " + " + b + " = ?";
                expected = a + b;
            } else {
                int max = Math.max(a, b);
                int min = Math.min(a, b);
                question = max + " - " + min + " = ?";
                expected = max - min;
            }
        } else if (difficulty <= 4) {
            int a = random.nextInt(14) + 2;
            int b = random.nextInt(14) + 2;
            int c = random.nextInt(10) + 1;
            boolean useMultOrDiv = random.nextBoolean();

            if (useMultOrDiv) {
                boolean mult = random.nextBoolean();
                if (mult) {
                    question = a + " * " + b + " = ?";
                    expected = a * b;
                } else {
                    int product = a * b;
                    question = product + " / " + a + " = ?";
                    expected = b;
                }
            } else {
                boolean firstAdd = random.nextBoolean();
                boolean secondAdd = random.nextBoolean();

                int firstResult;
                if (firstAdd) {
                    firstResult = a + b;
                    question = "(" + a + " + " + b + ")";
                } else {
                    int max = Math.max(a, b);
                    int min = Math.min(a, b);
                    firstResult = max - min;
                    question = "(" + max + " - " + min + ")";
                }

                if (secondAdd) {
                    question += " + " + c + " = ?";
                    expected = firstResult + c;
                } else {
                    if (firstResult >= c) {
                        question += " - " + c + " = ?";
                        expected = firstResult - c;
                    } else {
                        String inner = firstAdd ? (a + " + " + b) : (Math.max(a, b) + " - " + Math.min(a, b));
                        question = c + " - (" + inner + ") = ?";
                        expected = c - firstResult;
                    }
                }
            }
        } else {
            int a = random.nextInt(50) + 10;
            int b = random.nextInt(30) + 5;
            int c = random.nextInt(20) + 5;

            boolean firstIsMult = random.nextBoolean();
            boolean secondIsAdd = random.nextBoolean();

            int firstPart;
            String firstPartStr;
            if (firstIsMult) {
                firstPart = a * b;
                firstPartStr = a + " * " + b;
            } else {
                int prod = a * b;
                firstPart = prod / a;
                firstPartStr = prod + " / " + a;
            }

            if (secondIsAdd) {
                question = "(" + firstPartStr + ") + " + c + " = ?";
                expected = firstPart + c;
            } else {
                if (firstPart >= c) {
                    question = "(" + firstPartStr + ") - " + c + " = ?";
                    expected = firstPart - c;
                } else {
                    question = c + " - (" + firstPartStr + ") = ?";
                    expected = c - firstPart;
                }
            }
        }

        System.out.println(question);
        if (!scanner.hasNextInt()) {
            scanner.nextLine();
            return false;
        }
        int answer = scanner.nextInt();
        return answer == expected;
    }
}