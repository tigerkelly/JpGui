package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.rkw.IniFile;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class JpGlobal {

	private static JpGlobal singleton = null;
	
	private JpGlobal() {
		initGlobals();
	}
	
	private void initGlobals() {
		appVersion = "2.0.0";
		
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win") == true) {
			osType = 1;
		} else if (os.contains("nux") == true || 
				os.contains("nix") == true || 
				os.contains("aix") == true || 
				os.contains("sunos") == true) {
			osType = 2;
		} else if (os.contains("mac") == true) {
			osType = 3;
		}
		
		String d = System.getProperty("user.home");
		homeDir = new File(d + File.separator + ".JpGui");
		
		if (homeDir.exists() == false) {
			homeDir.mkdirs();
		}
		
//		System.out.println("homeDir: "+ homeDir.getAbsolutePath());
		
		workspace = new File(homeDir.getAbsolutePath() + File.separator + "ws");
		
		setupProject();
		
		prjList = new HashMap<String, IniFile>();
		orgList = new HashMap<String, IniFile>();
		
		prjList.clear();
		orgList.clear();
		
		sceneNav = new SceneNav();
		
		InputStream undoImg = getClass().getResourceAsStream("/images/undo.png");
		imgUndo = new Image(undoImg, 18, 18, false, false);
		InputStream dirImg = getClass().getResourceAsStream("/images/folder.png");
		imgDir = new Image(dirImg, 18, 18, false, false);
		InputStream fileImg = getClass().getResourceAsStream("/images/file_icon.png");
		imgFile = new Image(fileImg, 18, 18, false, false);
		InputStream helpImg = getClass().getResourceAsStream("/images/help_icon.png");
		imgHelp = new Image(helpImg, 18, 18, false, false);
		
		InputStream listImg = getClass().getResourceAsStream("/images/list.png");
		imgList = new Image(listImg, 18, 18, false, false);
		InputStream editImg = getClass().getResourceAsStream("/images/edit.png");
		imgEdit = new Image(editImg, 18, 18, false, false);
		
		font1 = Font.font("SansSerif", 16.0);
	}
	
	public String appVersion = null;
	public Map<String, IniFile> prjList = null;
	public Map<String, IniFile> orgList = null;
	
	public Image imgUndo = null;
	public Image imgDir = null;
	public Image imgFile = null;
	public Image imgHelp = null;
	public Image imgList = null;
	public Image imgEdit = null;
	
	public Font font1 = null;
	
	public boolean loadFlag = false;
	public boolean leaveProgram = false;
	public boolean rmAll = false;
	public boolean useBash = false;
	
	public int WINDOWS = 1;
	public int LINUX = 2;
	public int MAC = 3;
	
	Alert alert = null;
	public IniFile sysIni = null;
	public IniFile currPrj = null;
	public SceneNav sceneNav = null;
	public File workDir = null;
	public File homeDir = null;
	public File baseDir = null;
//	public File linuxBaseDir = null;
//	public File macBaseDir = null;
	public File workspace = null;
	public int osType = 0;
	
	public String projectOpen = null;
	public String dirsSelected = null;
	public String modulesSelected = null;
	
	public String jpackageVersion = null;
	public String jpackagePath = null;
	public String modulePath = null;
	public String platform = null;
	
	public Label status = null;
	
	public static JpGlobal getInstance() {
		// return SingletonHolder.singleton;
		if (singleton == null) {
			synchronized (JpGlobal.class) {
				singleton = new JpGlobal();
			}
		}
		return singleton;
	}
	
	public void setupProject() {
		workDir = new File(workspace.getAbsolutePath());
		
		if (workDir.exists() == false) {
			workDir.mkdirs();
		}
		
		File f = new File(workDir.getAbsolutePath() + File.separator + "jpgui.ini");
		
		if (f.exists() == false) {
			try {
				f.createNewFile();
				
				try {
					FileWriter myWriter = new FileWriter(f.getAbsolutePath());
					myWriter.write("# Jpackage project builder INI file.\n\n[System]\n");
					myWriter.write("\tjpackage = jpackage\n");
					myWriter.write("\tmodulepath = \n");
					myWriter.write("\tbasedir =\n");
					myWriter.write("\tusebash = false\n");
//					myWriter.write("\tLinux basedir =\n");
//					myWriter.write("\tMac basedir =\n\n");
						
					myWriter.write("[Projects]\n\n");
					myWriter.write("[UserMods]\n\n");
					myWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		sysIni = new IniFile(f.getAbsolutePath());
		
		jpackagePath = sysIni.getString("System", "jpackage");
		if (jpackagePath == null || jpackagePath.isBlank() == true)
			jpackagePath = "jpackage";
		
		String basedir = sysIni.getString("System", "basedir");
		if (basedir != null)
			baseDir = new File(basedir);
		
//		String linuxBase = sysIni.getString("System", "Linux basedir");
//		if (linuxBase != null)
//			linuxBaseDir = new File(linuxBase);
//		
//		String macBase = sysIni.getString("System", "Mac basedir");
//		if (macBase != null)
//			macBaseDir = new File(macBase);
		
		if (sysIni.keyExists("System", "removeall") == true)
			rmAll = sysIni.getBoolean("System", "removeAll");
		
		String cmd = jpackagePath + " --version";
		ProcessRet pr = runProcess(cmd.split(" "), null);
		jpackageVersion = pr.getOutput();
	}
	
	public String scenePeek() {
		if (sceneNav.sceneQue == null || sceneNav.sceneQue.isEmpty())
			return SceneNav.JPGUI;
		else
			return sceneNav.sceneQue.peek();
	}

	public void guiRestart(String msg) {
		String errMsg = String.format("A GUI error occurred.\r\nError loading %s\r\n\r\nRestarting GUI.", msg);
		showAlert("GUI Error", errMsg, AlertType.CONFIRMATION, false);
		System.exit(1);
	}

	public void loadSceneNav(String fxml) {
		if (sceneNav.loadScene(fxml) == true) {
			guiRestart(fxml);
		}
	}
	
	public void loadPcb() {
		
	}
	
	public void closeAlert() {
		if (alert != null) {
			alert.close();
			alert = null;
		}
	}
	
	public void addStatus(String msg) {
		if (status == null)
			return;
		String current = status.getText();
		status.setText(current + ", " + msg);
	}
	
	public void setStatus(String msg) {
		if (status == null)
			return;
		status.setText(msg);
	}
	
	public ButtonType yesNoAlert(String title, String msg, AlertType alertType) {
		ButtonType yes = new ButtonType("Yes", ButtonData.OK_DONE);
		ButtonType no = new ButtonType("No", ButtonData.CANCEL_CLOSE);
		Alert alert = new Alert(alertType, msg, yes, no);
		alert.getDialogPane().setPrefWidth(500.0);
		alert.setTitle(title);
		alert.setHeaderText(null);
		
		for (ButtonType bt : alert.getDialogPane().getButtonTypes()) {
			Button button = (Button) alert.getDialogPane().lookupButton(bt);
			button.setStyle("-fx-font-size: 16px;");
			button.setPrefWidth(100.0);
		}
		
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().add(getClass().getResource("myDialogs.css").toExternalForm());
		dialogPane.getStyleClass().add("myDialog");

		Optional<ButtonType> result = alert.showAndWait();
		
		return result.get();
	}

	public ButtonType showAlert(String title, String msg, AlertType alertType, boolean yesNo) {
		alert = new Alert(alertType);
		alert.getDialogPane().setPrefWidth(600.0);
		for (ButtonType bt : alert.getDialogPane().getButtonTypes()) {
			Button button = (Button) alert.getDialogPane().lookupButton(bt);
			if (yesNo == true) {
				if (button.getText().equals("Cancel"))
					button.setText("No");
				else if (button.getText().equals("OK"))
					button.setText("Yes");
			}
			button.setStyle("-fx-font-size: 16px;");
			button.setPrefWidth(100.0);
		}
		alert.setTitle(title);
		alert.setHeaderText(null);

		alert.setContentText(msg);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().add(getClass().getResource("myDialogs.css").toExternalForm());
		dialogPane.getStyleClass().add("myDialog");

		ButtonType bt = alert.showAndWait().get();

		alert = null;

		return bt;
	}
	
	public void showOutput(String title, String msg, AlertType alertType, boolean yesNo) {
		alert = new Alert(alertType);
		alert.getDialogPane().setPrefWidth(750.0);
		alert.getDialogPane().setPrefHeight(450.0);
		for (ButtonType bt : alert.getDialogPane().getButtonTypes()) {
			Button button = (Button) alert.getDialogPane().lookupButton(bt);
			if (yesNo == true) {
				if (button.getText().equals("Cancel"))
					button.setText("No");
				else if (button.getText().equals("OK"))
					button.setText("Yes");
			}
			button.setStyle("-fx-font-size: 16px;");
			button.setPrefWidth(100.0);
		}
		alert.setTitle(title);
		alert.setHeaderText(null);
	
		TextArea txt = new TextArea(msg);
		txt.setStyle("-fx-font-size: 16px;");
		txt.setWrapText(true);

		alert.getDialogPane().setContent(txt);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().add(getClass().getResource("myDialogs.css").toExternalForm());
		dialogPane.getStyleClass().add("myDialog");

		alert.showAndWait().get();

		alert = null;
	}

	public void Msg(String msg) {
		System.out.println(msg);
	}
	
	public boolean copyFile(File in, File out) {
		
		try {
	        FileInputStream fis  = new FileInputStream(in);
	        FileOutputStream fos = new FileOutputStream(out);
	        byte[] buf = new byte[4096];
	        int i = 0;
	        while((i=fis.read(buf))!=-1) {
	            fos.write(buf, 0, i);
	        }
	        fis.close();
	        fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return true;
		}
		
		return false;
    }
	
	public void deleteDir(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            deleteDir(f);
	        }
	    }
	    file.delete();
	}
	
	public void unzip(String zipFilePath, String destDir) {
		File dir = new File(destDir);
		// create output directory if it doesn't exist
		if (!dir.exists())
			dir.mkdirs();
		FileInputStream fis;
		// buffer for read and write data to file
		byte[] buffer = new byte[1024];
		try {
			fis = new FileInputStream(zipFilePath);
			ZipInputStream zis = new ZipInputStream(fis);
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				String fileName = ze.getName();
				File newFile = new File(destDir + File.separator + fileName);
//				System.out.println("Unzipping to " + newFile.getAbsolutePath());
				// create directories for sub directories in zip
				new File(newFile.getParent()).mkdirs();
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				// close this ZipEntry
				zis.closeEntry();
				ze = zis.getNextEntry();
			}
			// close last ZipEntry
			zis.closeEntry();
			zis.close();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void zipFile(String prjName, String prjDir, String zipFileName) throws IOException {
		final Path sourceDir = Paths.get(prjDir);
        try {
            final ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipFileName));
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                	String pn = file.toString();
                	// Omit the executable that was created.
                	if(pn.contains(File.separator + "win-out" + File.separator + prjName) == true) {
                		return FileVisitResult.CONTINUE;
                	} else if(pn.contains(File.separator + "linux-out" + File.separator + prjName) == true) {
                		return FileVisitResult.CONTINUE;
                	} else if(pn.contains(File.separator + "mac-out" + File.separator + prjName) == true) {
                		return FileVisitResult.CONTINUE;
                	}
                	
                    try {
                        Path targetFile = sourceDir.relativize(file);
                        outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                        byte[] bytes = Files.readAllBytes(file);
                        outputStream.write(bytes, 0, bytes.length);
                        outputStream.closeEntry();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public boolean isDigit(String s) {
		boolean tf = true;
		
		for (int i = 0; i < s.length(); i++) {
			if (Character.isDigit(s.charAt(i)) == false) {
				tf = false;
				break;
			}
		}
		
		return tf;
	}
	
	public void centerScene(Node node, String fxml, String title, String data) {
		FXMLLoader loader = null;
		try {
			Stage stage = new Stage();
			stage.setTitle(title);

			loader = new FXMLLoader(getClass().getResource(fxml));

			stage.initModality(Modality.APPLICATION_MODAL);

			stage.setScene(new Scene(loader.load()));
			stage.hide();

			Stage ps = (Stage) node.getScene().getWindow();

			ChangeListener<Number> widthListener = (observable, oldValue, newValue) -> {
				double stageWidth = newValue.doubleValue();
				stage.setX(ps.getX() + ps.getWidth() / 2 - stageWidth / 2);
			};
			ChangeListener<Number> heightListener = (observable, oldValue, newValue) -> {
				double stageHeight = newValue.doubleValue();
				stage.setY(ps.getY() + ps.getHeight() / 2 - stageHeight / 2);
			};

			stage.widthProperty().addListener(widthListener);
			stage.heightProperty().addListener(heightListener);

			// Once the window is visible, remove the listeners
			stage.setOnShown(e2 -> {
				stage.widthProperty().removeListener(widthListener);
				stage.heightProperty().removeListener(heightListener);
			});

			stage.showAndWait();

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public FXMLLoader loadScene(Node node, String fxml, String title, String data) {
		FXMLLoader loader = null;
		try {
			Stage stage = new Stage();
			stage.setTitle(title);

			loader = new FXMLLoader(getClass().getResource(fxml));

			stage.initModality(Modality.APPLICATION_MODAL);

			stage.setScene(new Scene(loader.load()));
			stage.hide();

			Stage ps = (Stage) node.getScene().getWindow();

			if (ps != null) {
				ChangeListener<Number> widthListener = (observable, oldValue, newValue) -> {
					double stageWidth = newValue.doubleValue();
					stage.setX(ps.getX() + ps.getWidth() / 2 - stageWidth / 2);
				};
				ChangeListener<Number> heightListener = (observable, oldValue, newValue) -> {
					double stageHeight = newValue.doubleValue();
					stage.setY(ps.getY() + ps.getHeight() / 2 - stageHeight / 2);
				};
	
				stage.widthProperty().addListener(widthListener);
				stage.heightProperty().addListener(heightListener);
	
				// Once the window is visible, remove the listeners
				stage.setOnShown(e2 -> {
					stage.widthProperty().removeListener(widthListener);
					stage.heightProperty().removeListener(heightListener);
				});
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		return loader;
	}
	
	public ButtonType yesNoCancelAlert(Node node, String title, String msg, Image icon) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(title);
	    alert.setContentText(msg);
	    alert.getButtonTypes().setAll(ButtonType.YES, 
	                                  ButtonType.NO);
	    
	    DialogPane dialogPane = alert.getDialogPane();
	    dialogPane.setPrefWidth(600.0);
		dialogPane.getStylesheets().add(getClass().getResource("myDialogs.css").toExternalForm());
		dialogPane.getStyleClass().add("myDialog");
		
	    return alert.showAndWait().get();
	}
	
//	public ButtonInfo centerDialog(Node node, String title, String msg, Image icon, ButtonInfo[] buttons) {
//		FXMLLoader loader = null;
//		YesNoOKController yno = null;
//		try {
//			Stage stage = new Stage();
//			stage.setTitle(title);
//
//			loader = new FXMLLoader(getClass().getResource("YesNoOK.fxml"));
//
//			stage.initModality(Modality.APPLICATION_MODAL);
//			stage.initStyle(StageStyle.UTILITY);
//			stage.setAlwaysOnTop(true);
//
//			stage.setScene(new Scene(loader.load()));
//			stage.hide();
//
//			Stage ps = (Stage) node.getScene().getWindow();
//			
//			yno = (YesNoOKController)loader.getController();
//			if (title != null)
//				yno.setTitle(title);
//			if (msg != null)
//				yno.setMessage(msg);
//			if (icon != null)
//				yno.setImage(icon);
//			if (buttons != null)
//				yno.addButtons(buttons);
//			
//			stage.setOnCloseRequest((e) -> e.consume());		// disable Stage close button.
//
//			ChangeListener<Number> widthListener = (observable, oldValue, newValue) -> {
//				double stageWidth = newValue.doubleValue();
//				stage.setX(ps.getX() + ps.getWidth() / 2 - stageWidth / 2);
//			};
//			ChangeListener<Number> heightListener = (observable, oldValue, newValue) -> {
//				double stageHeight = newValue.doubleValue();
//				stage.setY(ps.getY() + ps.getHeight() / 2 - stageHeight / 2);
//			};
//
//			stage.widthProperty().addListener(widthListener);
//			stage.heightProperty().addListener(heightListener);
//
//			// Once the window is visible, remove the listeners
//			stage.setOnShown(e2 -> {
//				stage.widthProperty().removeListener(widthListener);
//				stage.heightProperty().removeListener(heightListener);
//			});
//
//			stage.showAndWait();
//
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//		
//		return yno.getAction();
//	}
	
	public ProcessRet runProcess(String[] args, Object obj) {
    	Process p = null;
    	ProcessBuilder pb = new ProcessBuilder();
		pb.redirectErrorStream(true);
		pb.command(args);

		try {
			p = pb.start();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

//		@SuppressWarnings("resource")
		StreamGobbler inGobbler = new StreamGobbler(p.getInputStream(), obj);
		inGobbler.start();

		int ev = 0;
		try {
			ev = p.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return new ProcessRet(ev, inGobbler.getOutput());
    }
}
