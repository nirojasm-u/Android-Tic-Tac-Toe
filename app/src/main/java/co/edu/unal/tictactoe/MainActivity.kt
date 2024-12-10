package co.edu.unal.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import co.edu.unal.tictactoe.ui.theme.AndroidTicTacToeTutorial2Theme
import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import android.content.Context


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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicTacToeGame() {
    var board by remember { mutableStateOf(List(3) { MutableList(3) { "" } }) }
    var startingPlayer by remember { mutableStateOf("X") }
    var currentPlayer by remember { mutableStateOf(startingPlayer) }
    var winner by remember { mutableStateOf<String?>(null) }
    var isGameOver by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val preferences = context.getSharedPreferences("TicTacToePrefs", Context.MODE_PRIVATE)
    val editor = preferences.edit()

    var humanWins by remember { mutableStateOf(preferences.getInt("humanWins", 0)) }
    var cpuWins by remember { mutableStateOf(preferences.getInt("cpuWins", 0)) }
    var ties by remember { mutableStateOf(preferences.getInt("ties", 0)) }
    var difficulty by remember { mutableStateOf("Normal") }
    var isMenuExpanded by remember { mutableStateOf(false) }

    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showQuitDialog by remember { mutableStateOf(false) }

    val mediaPlayerX = remember { MediaPlayer.create(context, R.raw.move_x) }
    val mediaPlayerO = remember { MediaPlayer.create(context, R.raw.move_o) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val cellSize = if (isLandscape) {
        80.dp
    } else{
        100.dp
    }

    LaunchedEffect(board) {
        winner = checkWinner(board)
        if (winner != null || board.flatten().all { it.isNotEmpty() }) {
            isGameOver = true
            when (winner) {
                "X" -> {
                    humanWins++
                    editor.putInt("humanWins", humanWins).apply()
                }
                "O" -> {
                    cpuWins++
                    editor.putInt("cpuWins", cpuWins).apply()
                }
                null -> {
                    ties++
                    editor.putInt("ties", ties).apply()
                }
            }
        } else if (currentPlayer == "O") {
            delay(1500L)
            val move = findBestMove(board, difficulty)
            if (move != null) {
                board = board.mapIndexed { r, rowValues ->
                    rowValues.mapIndexed { c, cellValue ->
                        if (r == move.first && c == move.second) "O" else cellValue
                    }.toMutableList()
                }
                mediaPlayerO.start()
                currentPlayer = "X"
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Release MediaPlayers when composable is disposed
            mediaPlayerX.release()
            mediaPlayerO.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tic Tac Toe") },
                actions = {
                    IconButton(onClick = { isMenuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nuevo Juego") },
                            onClick = {
                                board = List(3) { MutableList(3) { "" } }
                                startingPlayer = "X"
                                currentPlayer = startingPlayer
                                isGameOver = false
                                winner = null
                                isMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Establecer Dificultad") },
                            onClick = {
                                isMenuExpanded = false
                                showDifficultyDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Salir") },
                            onClick = {
                                isMenuExpanded = false
                                showQuitDialog = true
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Scores Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Jugador: $humanWins", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("CPU: $cpuWins", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Empates: $ties", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Column(
                    modifier = Modifier
                        .weight(1f) // Adjust weight to share space evenly
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Game Board
                    Board(board = board, isGameOver = isGameOver, cellSize = cellSize) { row, col ->
                        if (!isGameOver && board[row][col].isEmpty() && currentPlayer == "X") {
                            board = board.mapIndexed { r, rowValues ->
                                rowValues.mapIndexed { c, cellValue ->
                                    if (r == row && c == col) "X" else cellValue
                                }.toMutableList()
                            }
                            mediaPlayerX.start()
                            currentPlayer = "O"
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Game Board
                Board(board = board, isGameOver = isGameOver, cellSize = cellSize) { row, col ->
                    if (!isGameOver && board[row][col].isEmpty() && currentPlayer == "X") {
                        board = board.mapIndexed { r, rowValues ->
                            rowValues.mapIndexed { c, cellValue ->
                                if (r == row && c == col) "X" else cellValue
                            }.toMutableList()
                        }
                        mediaPlayerX.start()
                        currentPlayer = "O"
                    }
                }

                // Scores below the board
                Text(
                    text = "Jugador: $humanWins | CPU: $cpuWins | Empates: $ties",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }

    if (showDifficultyDialog) {
        DifficultyDialog(
            onDismiss = { showDifficultyDialog = false },
            onDifficultySelected = { selectedDifficulty ->
                difficulty = selectedDifficulty
                showDifficultyDialog = false
            }
        )
    }

    // Show the Quit Dialog if needed
    if (showQuitDialog) {
        QuitDialog(
            onDismiss = { showQuitDialog = false },
            onConfirmQuit = { System.exit(0) }
        )
    }

}


@Composable
fun Board(board: List<List<String>>, isGameOver: Boolean, cellSize: Dp, onCellClick: (Int, Int) -> Unit) {
    Column {
        board.forEachIndexed { row, rowValues ->
            Row(horizontalArrangement = Arrangement.Center) {
                rowValues.forEachIndexed { col, cellValue ->
                    Box(
                        modifier = Modifier
                            .size(cellSize)
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
                                when(board[row][col]){
                                    "X" -> Image(
                                        painter = painterResource(id = R.drawable.x_image),
                                        contentDescription = "X",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    "O" -> Image(
                                        painter = painterResource(id = R.drawable.o_image),
                                        contentDescription = "O",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    else -> Spacer(modifier = Modifier.fillMaxSize())
                                }
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

fun findBestMove(board: List<MutableList<String>>, difficulty: String): Pair<Int, Int>? {
    return when (difficulty){
        "Fácil" -> findRandomMove(board)
        "Normal" -> if ((0..1).random() == 0) findRandomMove(board) else minimaxMove(board)
        "Difícil" -> minimaxMove(board)
        else -> minimaxMove(board)
    }
}

fun findRandomMove(board: List<MutableList<String>>): Pair<Int, Int>? {
    val emptyCells = board.flatMapIndexed { row, rowValues ->
        rowValues.mapIndexedNotNull { col, cellValue ->
            if (cellValue.isEmpty()) row to col else null
        }
    }
    return if (emptyCells.isNotEmpty()) emptyCells.random() else null
}

fun minimaxMove(board: List<MutableList<String>>): Pair<Int, Int>? {
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

@Composable
fun DifficultyDialog(onDismiss: () -> Unit, onDifficultySelected: (String) -> Unit) {
    var selectedDifficulty by remember { mutableStateOf("Normal") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Establecer Dificultad") },
        text = {
            Column {
                listOf("Fácil", "Normal", "Difícil").forEach { difficulty ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (difficulty == selectedDifficulty),
                                onClick = { selectedDifficulty = difficulty }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (difficulty == selectedDifficulty),
                            onClick = { selectedDifficulty = difficulty }
                        )
                        Text(text = difficulty, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onDifficultySelected(selectedDifficulty) }) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun QuitDialog(onDismiss: () -> Unit, onConfirmQuit: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Salir") },
        text = { Text("Está seguro de que desea salir?") },
        confirmButton = {
            Button(onClick = onConfirmQuit) {
                Text("Salir")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

