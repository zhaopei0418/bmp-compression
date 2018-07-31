package com.github.bmpcompression;

import com.github.bmpcompression.util.BMPUtil;
import com.github.bmpcompression.util.CompressUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

@SpringBootApplication
public class BmpCompressionApplication extends JFrame {

	private File openedFile;


	public BmpCompressionApplication() throws HeadlessException {
	    initUi();
	}

	/**
	 * 初始化界面
	 */
	private void initUi() {
		this.createMenu();
		setTitle("压缩或者解压BMP图片");
		setSize(800, 600);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	/**
	 * 创建菜单栏
	 */
	private void createMenu() {
		JMenuBar menuBar = new JMenuBar();
		JFrame self = this;

		// 创建并添加文件菜单
		JMenu fileMenu = new JMenu("文件(F)");
		fileMenu.setMnemonic('F');
		JMenuItem itemOpenMenu = new JMenuItem("打开");
		JMenuItem itemSaveMenu = new JMenuItem("压缩");

		itemOpenMenu.setMnemonic('O');
		itemOpenMenu.addActionListener((ActionEvent event) -> {
			selectBmpFile();
			if (openedFile.getName().toLowerCase().endsWith(".bmp")) {
				itemSaveMenu.setEnabled(true);
				if (!BMPUtil.is24TrueBmp(openedFile)) {
					JOptionPane.showMessageDialog(self, "选择的文件不是24位位图!");
				} else if (!BMPUtil.vaild(openedFile)){
					JOptionPane.showMessageDialog(self, "位图宽，高小于等于0，或者大于2048，或者不是8的倍数!");
				} else {
					BMPUtil.resize(self, openedFile);
				}
			} else {
				itemSaveMenu.setEnabled(false);
				CompressUtil.readAndShow7zFile(self, openedFile);
			}
		});

		fileMenu.add(itemOpenMenu);
		itemSaveMenu.setMnemonic('S');
		itemSaveMenu.addActionListener((ActionEvent event) -> {
			double compressRatio = CompressUtil.compress7zFile(openedFile);
			JOptionPane.showMessageDialog(self, "压缩比率是:[" + String.format("%.2f", compressRatio) + "%]");
		});
		fileMenu.add(itemSaveMenu);
		menuBar.add(fileMenu);

		this.setJMenuBar(menuBar);
	}

	/**
	 * 选择文件
	 */
	private void selectBmpFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				String name = f.getName();
				return f.isDirectory() || name.toLowerCase().endsWith(".bmp")
						|| name.toLowerCase().endsWith(".im3");
			}

			@Override
			public String getDescription() {
				return "位图文件或者压缩文件(*.bmp;*.BMP;*.IM3)";
			}
		});
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.showDialog(new JLabel(), "选择bmp文件或者IM3压缩文件");
		this.openedFile = fileChooser.getSelectedFile();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		BMPUtil.paint(g);
	}

	public static void main(String[] args) {
		//SpringApplication.run(BmpCompressionApplication.class, args);
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(BmpCompressionApplication.class)
				.headless(false).run(args);

		EventQueue.invokeLater(() -> {
			BmpCompressionApplication ex = ctx.getBean(BmpCompressionApplication.class);
			ex.setVisible(true);
		});
	}
}
