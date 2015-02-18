marctv
===
##概要
WordpressでYoutubeやVimeoを再生するプラグイン

##使い方
1. 管理画面の"インストール済みプラグイン"から"MarcTV Video Embed"を有効化
2. 管理画面の"投稿"->"新規追加"でテキストモードにして  
\<a class="embedvideo" href=Hoge\>タイトル\</a\>  
のようなHTMLを埋め込み読み込むビデオを指定
3. サイトを表示

##Wordpress管理者アカウント
name:test pass:test

##WebDriver Tips
1.クリック  
driver.findElement(By.xpath("hoge")).click();  

2.ホバー    
// アクション：一連のイベントを作成
Actions actions = new Actions(driver);    
// 移動先の要素を取得  
WebElement target = driver.findElement(By.xpath("hoge"));  
// ホバーイベントを登録  
actions.moveToElement(target);  
// アクション実行
actions.perform();  

3.リサイズ  
// 指定のウィンドウサイズに変更  
int width = 1000;  
int height = 500;  
driver.manage().window().setSize(new Dimension(width, height));  

// 最大化  
driver.manage().window().maximize();  
