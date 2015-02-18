package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class MarcTVMutationTest {
	FirefoxDriver driver;
	String baseUrl = "http://localhost/wordpress";

	@Before
	public void initDriver() throws Exception {
		Proxy proxy = new Proxy().setHttpProxy("localhost:8080");
		DesiredCapabilities cap = new DesiredCapabilities();
		cap.setCapability(CapabilityType.PROXY, proxy);
		driver = new FirefoxDriver(cap);
		//driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
	}
	
	public void initDatabase() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost/",
				"root", "root");
		Statement stmt = con.createStatement();
		String sqlStr = "drop database wordpress";
		stmt.executeUpdate(sqlStr);
		sqlStr = "create database wordpress";
		stmt.executeUpdate(sqlStr);
		stmt.close();
		con.close();
		Runtime.getRuntime()
				.exec(new String[] {
						"sh",
						"-c",
						"mysql -u root -proot wordpress < /home/ubuntu/marctv/sql/wordpress.sql" });
	}

	@After
	public void close() {
		driver.get("http://localhost/jscoverage.html");

		new WebDriverWait(driver, 1).until(ExpectedConditions
				.elementToBeClickable(By.id("storeTab")));
		driver.findElement(By.id("storeTab")).click();

		new WebDriverWait(driver, 10)
				.until(ExpectedConditions.textToBePresentInElementLocated(
						By.id("progressLabel"), "Done"));
		new WebDriverWait(driver, 10).until(ExpectedConditions
				.elementToBeClickable(By.id("storeButton")));
		driver.findElement(By.id("storeButton")).click();
		new WebDriverWait(driver, 10).until(ExpectedConditions
				.textToBePresentInElementLocated(By.id("storeDiv"),
						"Coverage data stored at"));
		driver.quit();
	}

	@Test
	public void cropHover() throws Exception {
		driver.get(baseUrl);
		driver.executeScript("jQuery('.crop').mouseover();");
		driver.executeScript("jQuery('.crop').mouseout();");
	}
	
	@Test
	public void playiconHover() throws Exception {
		driver.get(baseUrl);
		driver.executeScript("jQuery('.playicon_area').mouseover();");
		driver.executeScript("jQuery('.playicon_area').mouseout();");
	}

	@Test
	public void playYoutube() throws Exception {
		driver.get(baseUrl);
		driver.findElementByXPath("//*[@id=\"post-15\"]/div/p[1]/div/div/span[2]").click();
		Thread.sleep(10000);
	}
	
	@Test
	public void playVimeo() throws Exception {
		driver.get(baseUrl);
		driver.findElementByXPath("//*[@id=\"post-15\"]/div/p[2]/div/div/span[2]").click();
	}
	
	@Test
	public void resize() throws Exception {
		driver.get(baseUrl);
		int width = 400;
		int height = 600;
		driver.manage().window().setSize(new Dimension(width, height));
		Thread.sleep(5000);
	}
}
