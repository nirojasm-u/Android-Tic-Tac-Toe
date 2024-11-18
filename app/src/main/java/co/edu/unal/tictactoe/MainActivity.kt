package co.edu.unal.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.unal.tictactoe.ui.theme.AndroidTicTacToeTutorial2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidTicTacToeTutorial2Theme {
                TicTacToeGame()
            }
        }
    }
}

@Composable
fun TicTacToeGame() {
    var board by remember { mutableStateOf(List(3) { MutableList(3) { "" } }) }
    var startingPlayer by remember { mutableStateOf("X") }
    var currentPlayer by remember { mutableStateOf(startingPlayer) }
    var winner by remember { mutableStateOf<String?>(null) }
    var isGameOver by remember { mutableStateOf(false) }

    var humanWins by remember { mutableStateOf(0) }
    var cpuWins by remember { mutableStateOf(0) }
    var ties by remember { mutableStateOf(0) }

    LaunchedEffect(board) {
        winner = checkWinner(board)
        if (winner != null || board.flatten().all { it.isNotEmpty() }) {
            isGameOver = true
            when (winner){
                "X" -> humanWins++
                "O" -> cpuWins++
                null -> ties++
            }
        } else if (currentPlayer == "O") {
            val move = findBestMove(board)
            if (move != null) {
                board[move.first][move.second] = "O"
                currentPlayer = "X"
            }
        }
    }

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isGameOver) {
                    winner?.let { "Ganador: $it" } ?: "Empate!"
                } else {
                    "Turno: $currentPlayer"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            Board(board, isGameOver) { row, col ->
                if (!isGameOver && board[row][col].isEmpty() && currentPlayer == "X") {
                    board = board.mapIndexed { r, rowValues ->
                        rowValues.mapIndexed { c, cellValue ->
                            if (r == row && c == col) "X" else cellValue
                        }.toMutableList()
                    }
                    currentPlayer = "O"
                }
            }

            Text(
                text = "Jugador 1: $humanWins | CPU: $cpuWins | Empates: $ties",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )

            if (isGameOver) {
                Button(
                    onClick = {
                        board = List(3) { MutableList(3) { "" } }
                        startingPlayer = if (startingPlayer == "X") "O" else "X"
                        currentPlayer = startingPlayer
                        isGameOver = false
                        winner = null
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Reiniciar")
                }
            }
        }
    }
}

@Composable
fun Board(board: List<List<String>>, isGameOver: Boolean, onCellClick: (Int, Int) -> Unit) {
    Column {
        board.forEachIndexed { row, rowValues ->
            Row(horizontalArrangement = Arrangement.Center) {
                rowValues.forEachIndexed { col, cellValue ->
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .padding(4.dp)
                            .clickable(enabled = cellValue.isEmpty() && !isGameOver) {
                                onCellClick(row, col)
                            },
                        contentAlignment = Alignment.Center // Center content in the box
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.LightGray
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center // Center content inside the surface
                            ) {
                                Text(
                                    text = cellValue,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun checkWinner(board: List<List<String>>): String? {
    val lines = listOf(
        // Rows
        listOf(0 to 0, 0 to 1, 0 to 2),
        listOf(1 to 0, 1 to 1, 1 to 2),
        listOf(2 to 0, 2 to 1, 2 to 2),
        // Columns
        listOf(0 to 0, 1 to 0, 2 to 0),
        listOf(0 to 1, 1 to 1, 2 to 1),
        listOf(0 to 2, 1 to 2, 2 to 2),
        // Diagonals
        listOf(0 to 0, 1 to 1, 2 to 2),
        listOf(0 to 2, 1 to 1, 2 to 0)
    )
    for (line in lines) {
        val (a, b, c) = line
        if (board[a.first][a.second].isNotEmpty() &&
            board[a.first][a.second] == board[b.first][b.second] &&
            board[a.first][a.second] == board[c.first][c.second]
        ) {
            return board[a.first][a.second]
        }
    }
    return null
}

fun findBestMove(board: List<MutableList<String>>): Pair<Int, Int>? {
    var bestMove: Pair<Int, Int>? = null
    var bestScore = Int.MIN_VALUE
    for (row in board.indices) {
        for (col in board[row].indices) {
            if (board[row][col].isEmpty()) {
                board[row][col] = "O"
                val score = minimax(board, 0, false)
                board[row][col] = ""
                if (score > bestScore) {
                    bestScore = score
                    bestMove = row to col
                }
            }
        }
    }
    return bestMove
}

fun minimax(board: List<MutableList<String>>, depth: Int, isMaximizing: Boolean): Int {
    val winner = checkWinner(board)
    if (winner == "O") return 10 - depth
    if (winner == "X") return depth - 10
    if (board.flatten().all { it.isNotEmpty() }) return 0

    if (isMaximizing) {
        var maxEval = Int.MIN_VALUE
        for (row in board.indices) {
            for (col in board[row].indices) {
                if (board[row][col].isEmpty()) {
                    board[row][col] = "O"
                    val eval = minimax(board, depth + 1, false)
                    board[row][col] = ""
                    maxEval = maxOf(maxEval, eval)
                }
            }
        }
        return maxEval
    } else {
        var minEval = Int.MAX_VALUE
        for (row in board.indices) {
            for (col in board[row].indices) {
                if (board[row][col].isEmpty()) {
                    board[row][col] = "X"
                    val eval = minimax(board, depth + 1, true)
                    board[row][col] = ""
                    minEval = minOf(minEval, eval)
                }
            }
        }
        return minEval
    }
}
