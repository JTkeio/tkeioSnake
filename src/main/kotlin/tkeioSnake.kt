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

import io.battlesnake.core.AbstractBattleSnake
import io.battlesnake.core.DOWN
import io.battlesnake.core.DescribeResponse
import io.battlesnake.core.GameStrategy
import io.battlesnake.core.LEFT
import io.battlesnake.core.MoveRequest
import io.battlesnake.core.MoveResponse
import io.battlesnake.core.RIGHT
import io.battlesnake.core.SnakeContext
import io.battlesnake.core.StartRequest
import io.battlesnake.core.UP
import io.battlesnake.core.strategy
import io.ktor.application.ApplicationCall
import jtkeio.brain.Brain

public fun buildSnake(context: TkeioSnake.MySnakeContext, request: StartRequest) {
    val snakeBrain = Brain(Array((request.board.height*request.board.width) + 1){if (it>(request.board.height*request.board.width) - 1) (7) else (100)}, arrayOf(1,1,1,1))
    snakeBrain.searchAlgorithm = {da, sg -> snakeBrain.generateNeuronProximityAverageProbability(da, sg)}
}

fun translateMove(moveSet: Array<Int>): String {
    return "up"
}

fun grabMove(context: SnakeContext, request: MoveRequest): MoveResponse {
    return LEFT
}

//I tried putting the functions up here so that I had room to do calculations before giving the answer

object TkeioSnake : AbstractBattleSnake<TkeioSnake.MySnakeContext>() {

  override fun gameStrategy(): GameStrategy<MySnakeContext> =
    strategy(verbose = true) {

      onDescribe { call: ApplicationCall ->
        DescribeResponse("jtkeio", "#000000", "beluga", "bolt")
      }

      onStart { context: MySnakeContext, request: StartRequest ->
        val you = request.you
        val board = request.board
      }

      onMove { context: MySnakeContext, request: MoveRequest ->
          grabMove(context, request)
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