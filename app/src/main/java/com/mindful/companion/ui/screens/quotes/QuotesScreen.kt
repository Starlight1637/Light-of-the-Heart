package com.mindful.companion.ui.screens.quotes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesScreen(
    navController: NavController
) {
    var currentQuote by remember { mutableStateOf(getRandomQuote()) }
    var visible by remember { mutableStateOf(true) }
    
    // 监听 visible 变化，延迟后显示新句子
    LaunchedEffect(visible) {
        if (!visible) {
            kotlinx.coroutines.delay(300)
            currentQuote = getRandomQuote()
            visible = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("暖心词句") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    visible = false
                }
            ) {
                Icon(Icons.Default.Refresh, "换一句")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💝",
                            style = MaterialTheme.typography.displayLarge
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = currentQuote.text,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = MaterialTheme.typography.headlineSmall.lineHeight * 1.5
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "— ${currentQuote.author}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

data class Quote(
    val text: String,
    val author: String
)

private fun getRandomQuote(): Quote {
    val quotes = listOf(
        Quote("你值得被温柔以待，也值得拥有所有美好的事物。", "心光"),
        Quote("每一次呼吸，都是重新开始的机会。", "心光"),
        Quote("你不必完美，只需要做你自己。", "心光"),
        Quote("今天的你，已经很努力了。", "心光"),
        Quote("允许自己休息，这不是懒惰，而是自我关怀。", "心光"),
        Quote("你的感受是真实的，值得被看见和理解。", "心光"),
        Quote("每一个小小的进步，都值得被庆祝。", "心光"),
        Quote("你比你想象的更坚强，更勇敢。", "心光"),
        Quote("给自己一些时间，一切都会好起来的。", "心光"),
        Quote("你的存在本身，就是一种价值。", "心光"),
        Quote("不要害怕寻求帮助，这是勇气的表现。", "心光"),
        Quote("今天可能很难，但你依然在前进。", "心光"),
        Quote("你的情绪没有对错，它们只是在告诉你一些事情。", "心光"),
        Quote("慢一点也没关系，重要的是你在路上。", "心光"),
        Quote("你已经做得很好了，真的。", "心光"),
        Quote("黑暗终将过去，黎明总会到来。", "心光"),
        Quote("你不是一个人在战斗。", "心光"),
        Quote("善待自己，就像善待你最好的朋友一样。", "心光"),
        Quote("每一天都是新的开始。", "心光"),
        Quote("你的心灵需要休息，就像身体需要睡眠一样。", "心光"),
        Quote("天凉了记得添衣，三餐要按时吃，生活再忙，也要照顾好自己 —— 你值得被岁月温柔以待。", "心光"),
        Quote("不用事事追求完美，累了就歇一歇，平凡日子里的踏实努力，已经很了不起了。", "心光"),
        Quote("不管今天遇到了什么，夜晚的星光和清晨的朝阳都会如期而至，给你重新出发的勇气。", "心光"),
        Quote("家人的牵挂、朋友的陪伴，都是藏在身边的小确幸，愿你永远被爱包围，满心温暖。", "心光"),
        Quote("一杯热水、一句问候、一个拥抱，生活中的小温暖，总能治愈所有疲惫。", "心光"),
        Quote("那些咬牙坚持的日子，终会变成照亮前路的光，你的努力，时间从来不会辜负。", "心光"),
        Quote("不必焦虑迷茫，每一步前行都有意义，哪怕走得慢一点，也是在靠近想要的生活。", "心光"),
        Quote("没有不可跨越的难关，没有不能治愈的伤痛，所有失去的，都会以另一种方式归来。", "心光"),
        Quote("你不必成为更好的别人，只需做更完整的自己，你的独特与真诚，本身就是宝藏。", "心光"),
        Quote("人生路上难免有坎坷，但总有不期而遇的温暖，和生生不息的希望等着你。", "心光"),
        Quote("委屈了就说出来，难过了就哭一场，不用假装坚强，总有人愿意为你撑起一片天。", "心光"),
        Quote("生活不会一直一帆风顺，但也不会一直乌云密布，心若向阳，处处都是阳光。", "心光"),
        Quote("别苛责自己，你已经做得很好了，那些不被理解的瞬间，终将成为成长的勋章。", "心光"),
        Quote("把烦恼清空，把快乐装满，日子虽然普通，但也能过得有滋有味、温暖惬意。", "心光"),
        Quote("你不是一个人在奋斗，身后有家人的支持、社会的保障，还有国家的守护，只管勇敢向前。", "心光")
    )
    
    return quotes[Random.nextInt(quotes.size)]
}
