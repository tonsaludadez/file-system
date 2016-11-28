import java.io.*;
import java.util.*;

class TreeNode implements java.io.Serializable{
  FileDescriptor info;
  TreeNode parent;
  ArrayList<TreeNode> children;

  TreeNode(){
    parent = null;
    children = new ArrayList<>();
    info = new FileDescriptor();
  }

  TreeNode(TreeNode parent, FileDescriptor info){
    this.parent = parent;
    this.info = info;
    children = new ArrayList<>();

    if(parent!=null){
    	parent.insertChild(this);
    }
  }

  void insertChild(TreeNode child){
    children.add(child);
  }

  void removeChild(TreeNode child){
    children.remove(child);
  }

  String getName(){
    return info.filename;
  }

  void setName(String new_name){
  	info.filename = new_name;
  }

  boolean isFolder(){
    return info.isDir;
  }

  String displayContent(){
  	return info.content;
  }

  void changeParent(TreeNode new_parent){
  	TreeNode old_parent = parent;
  	old_parent.removeChild(this);

  	parent = new_parent;
  	parent.insertChild(this);

  }

 void sortChildren(){
 	for(int i = 0; i < children.size(); i++){
 		for(int j = i+1; j <children.size(); j++){
 			if(children.get(i).getName().compareTo(children.get(j).getName()) > 0){
 				TreeNode temp = children.get(j);
 				children.set(j, children.get(i));
 				children.set(i, temp);
 			}
 		}
 		
 	}
 } 

}

class Tree implements java.io.Serializable{
  TreeNode root;

  Tree(){
    //root = new TreeNode();
  }

  static TreeNode search(String name, TreeNode curr){
  	if(curr.getName().equals(name) && curr.info.isDir){
  		return curr;
  	}

  	else{
  		for(int i = 0; i < curr.children.size(); i++){
  			return search(name, curr.children.get(i));
  		}
  	}

  	return null;
  }
}

class FileDescriptor implements java.io.Serializable{
  String filename;
  boolean isDir;
  String content;

  FileDescriptor(){
    filename = "root";
    isDir = true;
    content = null;
  }

  FileDescriptor(String filename, boolean isDir){
    this.filename = filename;
    this.isDir = isDir;

    if(isDir){
    	this.setContent("");
    }
  }

  void setContent(String new_content){
  	content = new_content;
  }

  void appendContent(String new_content){
  	content += new_content;
  }

  void displayFileInfo(){
    System.out.println("Name : " + filename);
    System.out.print("File type : ");
    if(isDir){
      System.out.print("Folder");
    }
    else{
      System.out.print("Text File");
    }
  }
}

class FileSystem implements java.io.Serializable{

  enum Command {
    CD, MKDIR, RMDIR, EXIT, LS, INVALID, EDIT, CP, SHOW, RN, MV
  }

  Tree tree;
  TreeNode current;
  Command command;
  String rest[];
  int editMode = 0;

  static FileOutputStream fo;
  static ObjectOutputStream out;

  FileSystem(){
    tree = new Tree();
    current = tree.root;
  }

  void printDirectory(TreeNode curr){
    if(curr.parent != null){
      printDirectory(curr.parent);
    }

    System.out.print(curr.getName());
      System.out.print("/");
  }

  void parseCommand(String command_line){
    String splittedString[] = command_line.split(" ");
    if(splittedString[0].equals(">") || splittedString[0].equals(">>")){
    	if(splittedString[0].equals(">")){
    		editMode = 1;
    	}
    	else if(splittedString[0].equals(">>")){
    		editMode = 2;
    	}

    	splittedString[0] = "edit";
    }

    if(inEnum(splittedString[0])){
      command = Command.valueOf(splittedString[0].toUpperCase());
      if(splittedString.length == 1){
        rest = null;
      }
      else{
        rest = Arrays.copyOfRange(splittedString, 1, splittedString.length);
      }
    }

    else{
      command = Command.valueOf("INVALID");
      rest = Arrays.copyOfRange(splittedString, 0, splittedString.length);
    }

    //for(int i = 0; i < rest.length; i++){
    //  System.out.println(rest[i]);
    //}
  }

  static boolean inEnum(String test) {

    for (Command c : Command.values()) {
        if (c.name().equals(test.toUpperCase())) {
            return true;
        }
    }

    return false;
  }

  void execute(){
    boolean found = false;
    boolean valid;

    try{
	    switch(command){
	      case CD:
	        if(rest != null){
	            String temp_rest[] = rest[0].split("/");
	            TreeNode temp_curr = current;

	            int k = 0;
	            if(temp_rest.length > 1){
	              if(temp_rest[1].equals("root")){
	                temp_curr = tree.root;
	                k = 2;
	                found = true;
	              }
	            }

	            for(; k < temp_rest.length; k++){
	              found = false;

	              if(temp_rest[k].equals("..")){
	                if(temp_curr != tree.root){
	                  temp_curr = temp_curr.parent;
	                }
	                found = true;
	              }

	              else{
	                if(temp_curr.children.isEmpty()){
	                  System.out.println("cd: " + rest[0] + ": No such file or directory");
	                  write("cd: " + rest[0] + ": No such file or directory");
	                  return;
	                }


	                for(int i = 0; i < temp_curr.children.size(); i++){
	                  if(temp_curr.children.get(i).getName().equals(temp_rest[k])){
	                    if(temp_curr.children.get(i).isFolder()){
	                      temp_curr = temp_curr.children.get(i);
	                      found = true;
	                      break;
	                    }
	                    else{
	                      System.out.println("cd: " + temp_rest[0] +": Not a directory");
	                      write("cd: " + temp_rest[0] +": Not a directory");
	                      return;
	                    }
	                  }

	                  if(found){
	                    break;
	                  }
	                }
	              }
	    
	            }

	            if(found){
	              current = temp_curr;
	              break;
	            }
	            System.out.println("cd: " + rest[0] + ": No such file or directory");
	            write("cd: " + rest[0] + ": No such file or directory");
	          
	        }
	        break;

	      case MKDIR:
	        if(rest != null){      
	          String temp_rest[] = rest[0].split("/");
	          TreeNode temp_curr = current;

	          if(temp_rest.length > 1){
	            int i = 0;

	            if(temp_rest[1].equals("root")){
	              temp_curr = tree.root;
	              valid = true;
	              i = 2;
	            }

	            for(; i < temp_rest.length - 1; i++){
	              valid = false;
	              
	              if(temp_rest[i].equals("..")){
	               	temp_curr = temp_curr.parent;
	                valid = true;
	              }

	              else{
		              for(int j = 0; j < temp_curr.children.size(); j++){

		                if(temp_curr.children.get(j).getName().equals(temp_rest[i]) && temp_curr.children.get(j).isFolder()){
		                  temp_curr = temp_curr.children.get(j);
		                  valid = true;
		                  break;
		                }
		              }
	              }

	              if(!valid){
	                System.out.println("mkdir: cannot create directory '" + rest[0] + "': No such file or directory");
	                write("mkdir: cannot create directory '" + rest[0] + "': No such file or directory");
	                return;
	              }
	            }

	            temp_rest[0] = temp_rest[temp_rest.length-1];
	          }

	          for(int i = 0; i < temp_curr.children.size(); i++){
	            if(temp_curr.children.get(i).getName().equals(temp_rest[0])){
	              System.out.println("mkdir: cannot create directory '" + rest[0] + "': File exists");
	              write("mkdir: cannot create directory '" + rest[0] + "': File exists");
	              return;
	            }
	          }

	          FileDescriptor temp_info = new FileDescriptor(temp_rest[0], true);
	          TreeNode temp_node = new TreeNode(temp_curr, temp_info);
	          temp_curr.sortChildren();
	          //temp_curr.insertChild(temp_node);
	        }

	        else{
	          System.out.println("mkdir: missing operand");
	          write("mkdir: missing operand");
	        }

	        break;

	      case RMDIR: 
	        if(rest != null){
	          String temp_rest[] = rest[0].split("/");
	          TreeNode temp_curr = current;

	          for(int j = 0; j < temp_rest.length; j++){

	            found = false;
	            for(int i = 0; i < temp_curr.children.size(); i++){
	              if(temp_curr.children.get(i).getName().equals(rest[0])){
	                TreeNode temp_node = temp_curr.children.get(i);
	                temp_node.parent = null;
	                temp_curr.removeChild(temp_node);
	                temp_node = null;
	                found = true;
	                break;
	              }
	            }
	          }

	          if(!found){
	            System.out.println("rmdir: failed to remove '" + rest[0] + "': No such file or directory");
	            write("rmdir: failed to remove '" + rest[0] + "': No such file or directory");
	          }
	        }

	        else{
	          System.out.println("rmdir: missing operand");
	          write("rmdir: missing operand");
	        }
	        break;

	      case EDIT:
	        if(rest != null){
	          String temp_rest[] = rest[0].split("/");
	          TreeNode temp_curr = current;

	          if(temp_rest.length > 1){
	            int i = 0;

	            if(temp_rest[1].equals("root")){
	              temp_curr = tree.root;
	              valid = true;
	              i = 2;
	            }

	            for(; i < temp_rest.length - 1; i++){
	              valid = false;

	              for(int j = 0; j < temp_curr.children.size(); j++){
	                if(temp_curr.children.get(j).getName().equals(temp_rest[i]) && temp_curr.children.get(j).isFolder()){
	                  temp_curr = temp_curr.children.get(j);
	                  valid = true;
	                  break;
	                }
	              }

	              if(!valid){
	                System.out.println("edit: cannot create file '" + rest[0] + "': No such directory");
	                write("edit: cannot create file '" + rest[0] + "': No such directory");
	                return;
	              }
	            }

	            temp_rest[0] = temp_rest[temp_rest.length-1]; 
	          }

	          FileDescriptor new_info;
	          TreeNode edit_node = null;

	          for(int i = 0; i < temp_curr.children.size(); i++){
	            if(temp_curr.children.get(i).getName().equals(temp_rest[0])){
	              edit_node = temp_curr.children.get(i);
	              if(editMode == 1){
	              	edit_node.info.setContent("");
	              }
	              //System.out.print(edit_node.displayContent().substring(4, edit_node.info.content.length()));
	              break;
	            }
	          }

	          if(edit_node == null){
	          	new_info = new FileDescriptor(temp_rest[0], false);
	          	edit_node = new TreeNode(temp_curr, new_info);
	          }

	          Scanner scan = new Scanner(System.in);

	          String added = "";
	          String temp_add;
	          char[] _temp;

	          while(true){
	          	temp_add = scan.nextLine();
	          	_temp = temp_add.toCharArray();

		        if(_temp.length >= 2){
		          	if(_temp[_temp.length-2] == '$' && _temp[_temp.length-1] == '#'){
		          		added += temp_add.substring(0, temp_add.length()-2);
		          		break;
		          	}
		        }

		        added += temp_add;
		        added += "\n";
		      
	          }

	          edit_node.info.appendContent(added);
	          System.out.println();


	        }

	        break;

	      case CP:
	      	if(rest != null){
	      		if(rest.length<2){
	      			System.out.println("cp: missing destination file operand after '" + rest[0] +"'");
	      			write("cp: missing destination file operand after '" + rest[0] +"'");
	      			return;
	      		}

	      		String temp_origNode = rest[0];
	      		String temp_rest[] = rest[1].split("/");
	          	TreeNode temp_curr = current;
	          	TreeNode cp_origNode = null;
	          	boolean exists = false;
	          	int found_pos = 0;

	      		for(int i = 0; i < temp_curr.children.size(); i++){

	      			if(temp_curr.children.get(i).getName().equals(temp_origNode)){
	      				cp_origNode = temp_curr.children.get(i);
	      				break;
	      			}
	      		}

	      		if(cp_origNode == null){
	      			System.out.println("cp: cannot stat '" + temp_rest[0] +"' No such file or directory");
	      			write("cp: cannot stat '" + temp_rest[0] +"' No such file or directory");
	      			return;
	      		}

	      		else{
	      			
	      			if(temp_rest.length >= 2){
	      				int x = 0;

	      				if(temp_rest[1].equals("root")){
	      					temp_curr = tree.root;
	      					x = 2;
	      					found = true;
	      				}
	      				
		      			for(; x < temp_rest.length-1; x++){
		      				found = false;

		      				if(temp_rest[x].equals("..")){
		      					temp_curr = temp_curr.parent;
		      					found = true;
		      				}

		      				else{
			      				for(int j = 0; j < temp_curr.children.size(); j++){

			      					if(temp_curr.children.get(j).getName().equals(temp_rest[x]) && temp_curr.children.get(j).isFolder()){
			      						temp_curr = temp_curr.children.get(j);
			      						found = true;

			      						if(x == temp_rest.length-1){
			      							exists = true;
			      						}

			      						break;
			      					}
			      				}

		      				}

		      				if(!found){
		      					System.out.println("cp: cannot create regular file '" + rest[1] + "': No such file or directory");
		      					write("cp: cannot create regular file '" + rest[1] + "': No such file or directory");
		      					return;
		      				}
		      			}


		      		}

		      		else{
		      			for(int i = 0; i < temp_curr.children.size(); i++){
		      				if(temp_curr.children.get(i).getName().equals(temp_rest[temp_rest.length-1])){
		      					exists = true;
		      					found_pos = i;
		      					break;
		      				}
		      			}
		      		}

	      		}

		      	if(exists){
		      		temp_curr.removeChild(temp_curr.children.get(found_pos));
		      	}

	      		TreeNode cp_newNode;
		      	FileDescriptor cp_fileDescriptor = new FileDescriptor(temp_rest[temp_rest.length-1], cp_origNode.info.isDir);
		      	cp_fileDescriptor.setContent(cp_origNode.info.content);
		      	cp_newNode = new TreeNode(temp_curr, cp_fileDescriptor);

		      	temp_curr.sortChildren();
	      	}

	      	else{
	      		System.out.println("cp: missing file operand");
	      		write("cp: missing file operand");
	      	}
	      	break;

	      case EXIT: 
	        break;

	      case LS:
	      	if(rest == null){
		        if(current.children.size() > 0){
		          System.out.println();
		          for(int i = 0, j = 0; i < current.children.size(); i++, j++){
		            System.out.print(current.children.get(i).getName());
		            write(current.children.get(i).getName());
		            if(current.children.get(i).isFolder()){
		              System.out.print("/  ");
		            }
		            else{
		              System.out.print("  ");
		            }
		            if(j == 4){
		              System.out.println();
		              j = 0;
		            }
		          }
		          System.out.println();
		        }
		    }
		    else{
		    	String ls_string = rest[0];
		    	char[] ls_charArr = ls_string.toCharArray();
		    	int count = 0;

		    	for(int i = 0; i < ls_charArr.length; i++){
		    		if(ls_charArr[i] == '*'){
		    			count++;
		    		}
		    	}

		    	int[] pos = new int[count];

		    	for(int i = 0, j = 0; i < ls_charArr.length;i++){
		    		if(ls_charArr[i] == '*'){
		    			pos[j] = i;
		    			j++;
		    		}
		    	}

		    	if(count > 1){
					char[] ls_temp = Arrays.copyOfRange(ls_charArr, 1, ls_charArr.length-1);
		    		String ls_tempString = String.valueOf(ls_temp);

		    		for(int i = 0, j = 0; i < current.children.size(); i++){
		    			if(current.children.get(i).getName().contains(ls_tempString)){
		    				System.out.print(current.children.get(i).getName());
		    				write(current.children.get(i).getName());
			            		if(current.children.get(i).isFolder()){
			              			System.out.print("/  ");
			            		}
			            		else{
			              			System.out.print("  ");
			            		}
			            		if(j == 4){
			              			System.out.println();
			              			j = 0;
			            		}	
		    			}
		    		}
		    	}

		    	//only one *
		    	else if(count == 1){
		    		//start
		    		if(pos[0] == 0){
		    			char[] ls_temp = Arrays.copyOfRange(ls_charArr, 1, ls_charArr.length);
		    			String ls_tempString = String.valueOf(ls_temp);

		    			for(int i = 0, j = 0; i < current.children.size(); i++){
		    				if(current.children.get(i).getName().length() >= ls_temp.length){
			    				String temp_name = current.children.get(i).getName().substring(current.children.get(i).getName().length() - ls_temp.length, current.children.get(i).getName().length());
		
			    				if(temp_name.equals(ls_tempString)){
			    					System.out.print(current.children.get(i).getName());
			    					write(current.children.get(i).getName());
			            			if(current.children.get(i).isFolder()){
			              				System.out.print("/  ");
			            			}
			            			else{
			              				System.out.print("  ");
			            			}
			            			if(j == 4){
			              				System.out.println();
			              				j = 0;
			            			}
			    				}
		    				}
		    			}

		    		}

		    		//last
		    		else if(pos[0] == ls_charArr.length-1){
		    			char[] ls_temp = Arrays.copyOfRange(ls_charArr, 0, ls_charArr.length-1);
		    			String ls_tempString = String.valueOf(ls_temp);

		    			for(int i = 0, j = 0; i < current.children.size(); i++){
		    				if(current.children.get(i).getName().length() >= ls_temp.length){
			    				String temp_name = current.children.get(i).getName().substring(0, pos[0]);
		
			    				if(temp_name.equals(ls_tempString)){
			    					System.out.print(current.children.get(i).getName());
			    					write(current.children.get(i).getName());
			            			if(current.children.get(i).isFolder()){
			              				System.out.print("/  ");
			            			}
			            			else{
			              				System.out.print("  ");
			            			}
			            			if(j == 4){
			              				System.out.println();
			              				j = 0;
			            			}
			    				}
		    				}
		    			}
		    		}

		    		//somewhere in the middle
		    		else{
		    			System.out.println("not implemented");
		    			write("not implemented");
		    		}

		    	}

		    	System.out.println();
		    }

	        break;

	      case SHOW:
	      	 if(rest != null){
	          String temp_rest = rest[0];

	          TreeNode show_node = null;

	          for(int i = 0; i < current.children.size(); i++){
	            if(current.children.get(i).getName().equals(temp_rest)){
	              show_node = current.children.get(i);
	              //System.out.println(show_node.displayContent());
	              if(show_node.info.content.length() < 4){
	              	System.out.println(show_node.displayContent());
	              	write(show_node.displayContent());
	              }
	              else{
	              	System.out.print(show_node.displayContent().substring(4, show_node.info.content.length()));
	              	write(show_node.displayContent().substring(4, show_node.info.content.length()));
	              }
	              System.out.println();
	              return;
	            }
	          }

	          if(show_node == null){
	          	System.out.println("NOT FOUND");
	          	write("NOT FOUND");
	          }
	        }

	        else{
	        	System.out.println("show: missing file operand");
	        	write("show: missing file operand");
	        }

	        break;
	      case RN:
	      	if(rest != null){
	      		for(int i = 0; i < current.children.size(); i++){
	      			if(current.children.get(i).getName().equals(rest[0])){
	      				current.children.get(i).setName(rest[1]);
	      				return;
	      			}
	      		}

	      		System.out.println("rn: cannot rename file: No such file or directory");
	      		write("rn: cannot rename file: No such file or directory");
	      	}

	      	break;

	      case MV:
	      	if(rest != null && rest.length == 2){
	      		for(int i = 0; i < current.children.size(); i++){
	      			if(current.children.get(i).getName().equals(rest[0])){
	      				//System.out.println(rest[0]);
	      				for(int j = 0; j < current.children.size(); j++){
	      					if(current.children.get(j).getName().equals(rest[1])){
	      						current.children.get(i).changeParent(current.children.get(j));
	      						return;
	      					}
	      				}

	      				FileDescriptor new_descriptor = new FileDescriptor(rest[1], true);
	      				TreeNode new_node = new TreeNode(current, new_descriptor);

	      				current.children.get(i).changeParent(new_node);
	      				current.sortChildren();
	      				return;
	      			}
	      		}
	      		System.out.println("mv: not executed");
	      		write("mv: not executed");
	        }

	      	else{
	      		System.out.println("mv: missing file operand");
	      		write("mv: missing file operand");
	      	}
	      	break;

	      case INVALID:
	        System.out.println(rest[0] + ": command not found");
	        write(rest[0] + ": command not found");
	        break;
	    }
	}
	catch(IOException e){
		return;
	}
  }

  public static void main(String[] args) throws FileNotFoundException, IOException{
    FileSystem fs = new FileSystem();
    fs.deserialize();
    fo = new FileOutputStream("FileSystem.ser");
    out = new ObjectOutputStream(fo);

    //current.info.displayFileInfo();

    fs.readFile();
    //fs.manualInput();

    fs.serialize(fs.tree.root);

    out.close();
  	fo.close();
  }

  void manualInput(){
	Scanner scan = new Scanner(System.in);

    while(command != Command.valueOf("EXIT")){
      printDirectory(current);
      System.out.print("\n$ ");
      String command_line = scan.nextLine(); 

      parseCommand(command_line);

      execute();
      System.out.println();
  	}
  }

  void readFile(){
  	String line = null;
  	String input_file = "mp3.in";

  	try{
  		FileReader fr = new FileReader(input_file);

  		BufferedReader br = new BufferedReader(fr);
  		int i = 0;

  		while((line = br.readLine()) != null) {
  			//System.out.println("LINE " + i + ": " + line);
  			parseCommand(line);
  			execute();
  			//System.out.println();
  			i++;
  		}
  	}
  	catch(FileNotFoundException ex) {
        System.out.println(
            "Unable to open file '" + 
            input_file + "'");                
    }

    catch(IOException ex) {
		ex.printStackTrace();
    }
  }

  void write(String line) throws IOException{
	FileWriter fw = null;
		
		try {
			fw = new FileWriter("mp3-2.out",true);
		    fw.write(line + "\n");
		    fw.close();
		} catch(NullPointerException e) {
			System.out.println("NullPointerException");
		}
  }

  void serialize(TreeNode curr) throws IOException{
  	out.writeObject(curr);
  	try{

  		for(int i = 0; i < curr.children.size(); i++){
  			if(curr.children.get(i).isFolder()){
  				serialize(curr.children.get(i));
  			}
  			else{
  				out.writeObject(curr);
  			}
  		}
  	}
  	catch(IOException i){

  		i.printStackTrace();
  	}
  }

  void deserialize(){
  	try {
         FileInputStream fileIn = new FileInputStream("FileSystem.ser");
         ObjectInputStream in = new ObjectInputStream(fileIn);
         TreeNode r = (TreeNode) in.readObject();
         tree.root = r;
         current = r;

         in.close();
         fileIn.close();
      }catch(IOException | ClassNotFoundException c) {
         tree.root = new TreeNode();
         current = tree.root;
         //c.printStackTrace();
         return;
      }
  }
  
}