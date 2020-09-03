/*
 * Copyright © 2020 Paul Ambrose (pambrose@mac.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

import io.battlesnake.core.*
import io.ktor.application.*
import jtkeio.brain.Brain

object TkeioSnake : AbstractBattleSnake<TkeioSnake.MySnakeContext>() {
    lateinit var snakeBrain: Brain
    var deadBool = false
    var changedMove = -1
    var maxMoves = 0.0
    var currentMoves = 0.0
    var lastMove = ""

    fun buildSnake(context: MySnakeContext, request: StartRequest) {
        this.snakeBrain = Brain(arrayOf(5), arrayOf(3))
        snakeBrain.read("C:/Users/Jacob Tkeio/Desktop/Programs/Kotlin Projects/${request.board.width}" + "x" + "${request.board.height}" + "Brain.txt")
        snakeBrain.searchAlgorithm = {da, sg -> snakeBrain.generateNeuronProximityPluralityAbsolute(da, sg)}
        currentMoves = 0.0
    } //get rid of the read or the old file if you change something

    fun translateMove(plannedMove: Int): String {
        val availableMoves = arrayOf("up", "right", "down", "left")
        return availableMoves[plannedMove]
    }

    fun grabMove(context: SnakeContext, request: MoveRequest): MoveResponse {
        val searchAddress = Array(1){0}
        val head = request.you.headPosition
        if (lastMove == "up") {
            searchAddress[0] = bubbleSearch(arrayOf(0, 1), request)
        } else if (lastMove == "down") {
            searchAddress[0] = bubbleSearch(arrayOf(0,-1), request)
        } else if (lastMove == "left") {
            searchAddress[0] = bubbleSearch(arrayOf(-1,0), request)
        } else {
            searchAddress[0] = bubbleSearch(arrayOf(1,0), request)
        }
        val moveString = translateMove(snakeBrain.pullNeuron(searchAddress, 1)[0])
        lastMove = moveString
        changedMove = snakeBrain.getLinear(searchAddress, snakeBrain.dimensions)
        currentMoves++
        return MoveResponse(moveString)
    }

    fun bubbleSearch(offset: Array<Int>, request: MoveRequest): Int {
        val x = request.you.headPosition.x + offset[0]
        val y = request.you.headPosition.y + offset[1]
        if (x<0 || x+1>request.board.width || y<0 || y+1>request.board.height) {
            return 4
        }
        val board = request.board
        for (j in board.snakes) {
            if (j.body.filter{it.x == x && it.y == y}.size == 1) {
                if (j.id == request.you.id) {
                    return 3
                } else {
                    return 2
                }
            }
        }
        for (food in board.food) {
            if (food.x == x && food.y == y) {
                return 1
            }
        }
        return 0
    }

    fun onFinish(request: EndRequest) {
        deadBool = false
        for (i in request.board.snakes) {
            if (i.id == request.you.id && request.you.health > 0) {
                deadBool = true
                break
            }
            if (i.health > 0) {
                deadBool = false
            }
        }
        if (deadBool || currentMoves > maxMoves) {
            maxMoves = currentMoves
            snakeBrain.store("C:/Users/Jacob Tkeio/Desktop/Programs/Kotlin Projects/${request.board.width}" + "x" + "${request.board.height}" + "Brain.txt")
            println()
            println("SNAKE WON; STORING INFORMATION")
            println()
        } else {
            snakeBrain.pushNeuron(snakeBrain.getDimensional(changedMove, snakeBrain.dimensions), arrayOf(-1))
            println()
            println("SNAKE LOST; ERASING LAST MOVE: ${snakeBrain.getDimensional(changedMove, snakeBrain.dimensions).joinToString(",")}")
            println()
            snakeBrain.store("C:/Users/Jacob Tkeio/Desktop/Programs/Kotlin Projects/${request.board.width}" + "x" + "${request.board.height}" + "Brain.txt")
            changedMove = -1
        }
    }

    override fun gameStrategy(): GameStrategy<MySnakeContext> = strategy(verbose = true) {

      onDescribe { call: ApplicationCall ->
        DescribeResponse("jtkeio", "#000000", "beluga", "bolt")
      }

      onStart { context: MySnakeContext, request: StartRequest ->
        val you = request.you
        val board = request.board
          buildSnake(context, request)
      }

      onMove { context: MySnakeContext, request: MoveRequest ->
          grabMove(context, request)
      }

        onEnd { context, request ->
            onFinish(request)
            EndResponse()
        }
    }

  class MySnakeContext : SnakeContext()
    override fun snakeContext(): MySnakeContext =
    MySnakeContext()



  @JvmStatic
  fun main(args: Array<String>) {
    run()
  }
}