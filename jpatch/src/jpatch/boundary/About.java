package jpatch.boundary;

import jpatch.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class About extends JDialog implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTabbedPane tabbedPane = new JTabbedPane();
	
	private JScrollPane paneAbout;
	private JScrollPane paneLicense;
	private JScrollPane pane3rdParty;
	private JScrollPane paneThanks;

	private String strAbout;
	private String strThanks;
	private String str3rdParty;
	private String strLicense;

	private JButton buttonOK = new JButton("OK",new ImageIcon(getClass().getClassLoader().getResource("jpatch/images/ok.png")));
	
	
	public About(Frame owner) {
		super(owner, "About JPatch...", true);
		
		strAbout =
			"<html>" +
			"<div align='center'><b>JPatch " + VersionInfo.version + "</b><br>" +
			"compiled " + VersionInfo.compileTime + "<br>" +
			"<br>" +
			"written by Sascha Ledinsky<br>" +
			"Copyright &copy; 2002-2005<br>"+ 
			"</div><p>" +
			"<i>This program is free software; you can redistribute it and/or modify " +
			"it under the terms of the GNU General Public License as published by " +
			"the Free Software Foundation; either version 2 of the License, or " +
			"(at your option) any later version. " +
			"<p>" +
			"This program is distributed in the hope that it will be useful, " +
			"but WITHOUT ANY WARRANTY; without even the implied warranty of " +
			"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the " +
			"GNU General Public License for more details. " +
			"<p>" +
			"You should have received a copy of the GNU General Public License " +
			"along with this program; if not, write to the Free Software " +
			"Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA</i>" +
			"</html>";
		
		str3rdParty =
			"<html>" +
			"JPatch can export RIB (RenderMan<font size='+2'>&reg;</font> Interface Bytestream), developed by Pixar.<br>" + 
			"<br><div align='center'><font face='monospace'>The RenderMan&reg; Interface Procedures and RIB Protocol are:<br>" +
			"Copyright 1988, 1989, Pixar.  All rights reserved.<br>" +
			"RenderMan&reg; is a registered trademark of Pixar.</font></div><br>" +
			"<hr><p>JPatch now includes <b>Inyo</b>, a renderer written by <i>David Cuny</i>. " +
			"See <a href='http://inyo.sourceforge.net'>http://inyo.sourceforge.net</a></p><br>" +
			"<hr><p>JPatch uses and contains binary distributions of:" +
			"<ul>" +
			"<li>An <b>'unofficial' Java3D vecmath package</b> by <i>Kenji Hirabane</i>. " +
			"See <a href='http://objectclub.esm.co.jp/vecmath/'>http://objectclub.esm.co.jp/vecmath/</a></li>" +
//			"<li>The <b>Xerces2 XML parser</b> Copyright 1999-2004 <i>The Apache Software Foundation</i>. " +
//			"See <a href='http://xml.apache.org/xerces2-j/index.html'>http://xml.apache.org/xerces2-j/index.html</a></li>" +
			"<li><b>BeanShell</b> by <i>Pat Niemeyer</i>. " +
			"See <a href='http://www.beanshell.org'>http://www.beanshell.org</a></li>" +
			"<li><b>Buoy</b> by <i>Peter Eastman</i>. " +
//			"See <a href='http://buoy.sourceforge.net'>http://buoy.sourceforge.net</a></li>" +
//			"<li><b>Inyo</b> by <i>David Cuny</i>. "+
			"See <a href='http://inyo.sourceforge.net'>http://inyo.sourceforge.net</a></li>" +
			"<li><b>The JOGL Java bindings for OpenGL</b>. "+
			"See <a href='https://jogl.dev.java.net/'>https://jogl.dev.java.net/</a></li>" +
			"</ul>" + 
			"The packages listed above have not been modified. For source code " +
			"or detailed licensing information please follow the links above. " +
			"I'd like to thank the authors of these great packages for making them freely available!</p>" +
			"</html>";
		
		
		strThanks =
			"<html>" +
			"Thanks to (in alphabetical order):<br>" +
			"<ul>" +
			"<li>Dan Bishop</li>" +
			"<li>David Cuny</li>" +
			"<li>Robert Hemby</li>" +
			"<li>Heiko Irrgang</li>" +
			"<li>Giovanni Martone</li>" +
			"<li>Gregg Patton</li>" +
			"</ul>" +
			"and everybody who participated in the forum, tested development snapshots " +
			"reported bugs, submitted ideas or gave feedback. Thanks a lot!" +
			"</html>";
		
		StringBuffer sbLicense = new StringBuffer();
		sbLicense.append("		    GNU GENERAL PUBLIC LICENSE                                  \n");
		sbLicense.append("		       Version 2, June 1991                                     \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append(" Copyright (C) 1989, 1991 Free Software Foundation, Inc.                      \n");
		sbLicense.append(" 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA                      \n");
		sbLicense.append(" Everyone is permitted to copy and distribute verbatim copies                 \n");
		sbLicense.append(" of this license document, but changing it is not allowed.                    \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("			    Preamble                                            \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("  The licenses for most software are designed to take away your               \n");
		sbLicense.append("freedom to share and change it.  By contrast, the GNU General Public          \n");
		sbLicense.append("License is intended to guarantee your freedom to share and change free        \n");
		sbLicense.append("software--to make sure the software is free for all its users.  This          \n");
		sbLicense.append("General Public License applies to most of the Free Software                   \n");
		sbLicense.append("Foundation's software and to any other program whose authors commit to        \n");
		sbLicense.append("using it.  (Some other Free Software Foundation software is covered by        \n");
		sbLicense.append("the GNU Library General Public License instead.)  You can apply it to         \n");
		sbLicense.append("your programs, too.                                                           \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("  When we speak of free software, we are referring to freedom, not            \n");
		sbLicense.append("price.  Our General Public Licenses are designed to make sure that you        \n");
		sbLicense.append("have the freedom to distribute copies of free software (and charge for        \n");
		sbLicense.append("this service if you wish), that you receive source code or can get it         \n");
		sbLicense.append("if you want it, that you can change the software or use pieces of it          \n");
		sbLicense.append("in new free programs; and that you know you can do these things.              \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("  To protect your rights, we need to make restrictions that forbid            \n");
		sbLicense.append("anyone to deny you these rights or to ask you to surrender the rights.        \n");
		sbLicense.append("These restrictions translate to certain responsibilities for you if you       \n");
		sbLicense.append("distribute copies of the software, or if you modify it.                       \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("  For example, if you distribute copies of such a program, whether            \n");
		sbLicense.append("gratis or for a fee, you must give the recipients all the rights that         \n");
		sbLicense.append("you have.  You must make sure that they, too, receive or can get the          \n");
		sbLicense.append("source code.  And you must show them these terms so they know their           \n");
		sbLicense.append("rights.                                                                       \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("  We protect your rights with two steps: (1) copyright the software, and      \n");
		sbLicense.append("(2) offer you this license which gives you legal permission to copy,          \n");
		sbLicense.append("distribute and/or modify the software.                                        \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("  Also, for each author's protection and ours, we want to make certain        \n");
		sbLicense.append("that everyone understands that there is no warranty for this free             \n");
		sbLicense.append("software.  If the software is modified by someone else and passed on, we      \n");
		sbLicense.append("want its recipients to know that what they have is not the original, so       \n");
		sbLicense.append("that any problems introduced by others will not reflect on the original       \n");
		sbLicense.append("authors' reputations.                                                         \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("  Finally, any free program is threatened constantly by software              \n");
		sbLicense.append("patents.  We wish to avoid the danger that redistributors of a free           \n");
		sbLicense.append("program will individually obtain patent licenses, in effect making the        \n");
		sbLicense.append("program proprietary.  To prevent this, we have made it clear that any         \n");
		sbLicense.append("patent must be licensed for everyone's free use or not licensed at all.       \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("  The precise terms and conditions for copying, distribution and              \n");
		sbLicense.append("modification follow.                                                          \n");
		sbLicense.append("                                                                             \n");
		sbLicense.append("		    GNU GENERAL PUBLIC LICENSE                                  \n");
		sbLicense.append("   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION            \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("  0. This License applies to any program or other work which contains         \n");
		sbLicense.append("a notice placed by the copyright holder saying it may be distributed          \n");
		sbLicense.append("under the terms of this General Public License.  The \"Program\", below,        \n");
		sbLicense.append("refers to any such program or work, and a \"work based on the Program\"         \n");
		sbLicense.append("means either the Program or any derivative work under copyright law:          \n");
		sbLicense.append("that is to say, a work containing the Program or a portion of it,             \n");
		sbLicense.append("either verbatim or with modifications and/or translated into another          \n");
		sbLicense.append("language.  (Hereinafter, translation is included without limitation in        \n");
		sbLicense.append("the term \"modification\".)  Each licensee is addressed as \"you\".               \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("Activities other than copying, distribution and modification are not          \n");
		sbLicense.append("covered by this License; they are outside its scope.  The act of              \n");
		sbLicense.append("running the Program is not restricted, and the output from the Program        \n");
		sbLicense.append("is covered only if its contents constitute a work based on the                \n");
		sbLicense.append("Program (independent of having been made by running the Program).             \n");
		sbLicense.append("Whether that is true depends on what the Program does.                        \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("  1. You may copy and distribute verbatim copies of the Program's             \n");
		sbLicense.append("source code as you receive it, in any medium, provided that you               \n");
		sbLicense.append("conspicuously and appropriately publish on each copy an appropriate           \n");
		sbLicense.append("copyright notice and disclaimer of warranty; keep intact all the              \n");
		sbLicense.append("notices that refer to this License and to the absence of any warranty;        \n");
		sbLicense.append("and give any other recipients of the Program a copy of this License           \n");
		sbLicense.append("along with the Program.                                                       \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("You may charge a fee for the physical act of transferring a copy, and         \n");
		sbLicense.append("you may at your option offer warranty protection in exchange for a fee.       \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("  2. You may modify your copy or copies of the Program or any portion         \n");
		sbLicense.append("of it, thus forming a work based on the Program, and copy and                 \n");
		sbLicense.append("distribute such modifications or work under the terms of Section 1            \n");
		sbLicense.append("above, provided that you also meet all of these conditions:                   \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("    a) You must cause the modified files to carry prominent notices           \n");
		sbLicense.append("    stating that you changed the files and the date of any change.            \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("    b) You must cause any work that you distribute or publish, that in        \n");
		sbLicense.append("    whole or in part contains or is derived from the Program or any           \n");
		sbLicense.append("    part thereof, to be licensed as a whole at no charge to all third         \n");
		sbLicense.append("    parties under the terms of this License.                                  \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("    c) If the modified program normally reads commands interactively          \n");
		sbLicense.append("    when run, you must cause it, when started running for such                \n");
		sbLicense.append("    interactive use in the most ordinary way, to print or display an          \n");
		sbLicense.append("    announcement including an appropriate copyright notice and a              \n");
		sbLicense.append("    notice that there is no warranty (or else, saying that you provide        \n");
		sbLicense.append("    a warranty) and that users may redistribute the program under             \n");
		sbLicense.append("    these conditions, and telling the user how to view a copy of this         \n");
		sbLicense.append("    License.  (Exception: if the Program itself is interactive but            \n");
		sbLicense.append("    does not normally print such an announcement, your work based on          \n");
		sbLicense.append("    the Program is not required to print an announcement.)                    \n");
		sbLicense.append("                                                                             \n");
		sbLicense.append("These requirements apply to the modified work as a whole.  If                 \n");
		sbLicense.append("identifiable sections of that work are not derived from the Program,          \n");
		sbLicense.append("and can be reasonably considered independent and separate works in            \n");
		sbLicense.append("themselves, then this License, and its terms, do not apply to those           \n");
		sbLicense.append("sections when you distribute them as separate works.  But when you            \n");
		sbLicense.append("distribute the same sections as part of a whole which is a work based         \n");
		sbLicense.append("on the Program, the distribution of the whole must be on the terms of         \n");
		sbLicense.append("this License, whose permissions for other licensees extend to the             \n");
		sbLicense.append("entire whole, and thus to each and every part regardless of who wrote it.     \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("Thus, it is not the intent of this section to claim rights or contest         \n");
		sbLicense.append("your rights to work written entirely by you; rather, the intent is to         \n");
		sbLicense.append("exercise the right to control the distribution of derivative or               \n");
		sbLicense.append("collective works based on the Program.                                        \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("In addition, mere aggregation of another work not based on the Program        \n");
		sbLicense.append("with the Program (or with a work based on the Program) on a volume of         \n");
		sbLicense.append("a storage or distribution medium does not bring the other work under          \n");
		sbLicense.append("the scope of this License.                                                    \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("  3. You may copy and distribute the Program (or a work based on it,          \n");
		sbLicense.append("under Section 2) in object code or executable form under the terms of         \n");
		sbLicense.append("Sections 1 and 2 above provided that you also do one of the following:        \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("    a) Accompany it with the complete corresponding machine-readable          \n");
		sbLicense.append("    source code, which must be distributed under the terms of Sections        \n");
		sbLicense.append("    1 and 2 above on a medium customarily used for software interchange; or,  \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("    b) Accompany it with a written offer, valid for at least three            \n");
		sbLicense.append("    years, to give any third party, for a charge no more than your            \n");
		sbLicense.append("    cost of physically performing source distribution, a complete             \n");
		sbLicense.append("    machine-readable copy of the corresponding source code, to be             \n");
		sbLicense.append("    distributed under the terms of Sections 1 and 2 above on a medium         \n");
		sbLicense.append("    customarily used for software interchange; or,                            \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("    c) Accompany it with the information you received as to the offer         \n");
		sbLicense.append("    to distribute corresponding source code.  (This alternative is            \n");
		sbLicense.append("    allowed only for noncommercial distribution and only if you               \n");
		sbLicense.append("    received the program in object code or executable form with such          \n");
		sbLicense.append("    an offer, in accord with Subsection b above.)                             \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("The source code for a work means the preferred form of the work for           \n");
		sbLicense.append("making modifications to it.  For an executable work, complete source          \n");
		sbLicense.append("code means all the source code for all modules it contains, plus any          \n");
		sbLicense.append("associated interface definition files, plus the scripts used to               \n");
		sbLicense.append("control compilation and installation of the executable.  However, as a        \n");
		sbLicense.append("special exception, the source code distributed need not include               \n");
		sbLicense.append("anything that is normally distributed (in either source or binary             \n");
		sbLicense.append("form) with the major components (compiler, kernel, and so on) of the          \n");
		sbLicense.append("operating system on which the executable runs, unless that component          \n");
		sbLicense.append("itself accompanies the executable.                                            \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("If distribution of executable or object code is made by offering              \n");
		sbLicense.append("access to copy from a designated place, then offering equivalent              \n");
		sbLicense.append("access to copy the source code from the same place counts as                  \n");
		sbLicense.append("distribution of the source code, even though third parties are not            \n");
		sbLicense.append("compelled to copy the source along with the object code.                      \n");
		sbLicense.append("                                                                             \n");
		sbLicense.append("  4. You may not copy, modify, sublicense, or distribute the Program          \n");
		sbLicense.append("except as expressly provided under this License.  Any attempt                 \n");
		sbLicense.append("otherwise to copy, modify, sublicense or distribute the Program is            \n");
		sbLicense.append("void, and will automatically terminate your rights under this License.        \n");
		sbLicense.append("However, parties who have received copies, or rights, from you under          \n");
		sbLicense.append("this License will not have their licenses terminated so long as such          \n");
		sbLicense.append("parties remain in full compliance.                                            \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("  5. You are not required to accept this License, since you have not          \n");
		sbLicense.append("signed it.  However, nothing else grants you permission to modify or          \n");
		sbLicense.append("distribute the Program or its derivative works.  These actions are            \n");
		sbLicense.append("prohibited by law if you do not accept this License.  Therefore, by           \n");
		sbLicense.append("modifying or distributing the Program (or any work based on the               \n");
		sbLicense.append("Program), you indicate your acceptance of this License to do so, and          \n");
		sbLicense.append("all its terms and conditions for copying, distributing or modifying           \n");
		sbLicense.append("the Program or works based on it.                                             \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("  6. Each time you redistribute the Program (or any work based on the         \n");
		sbLicense.append("Program), the recipient automatically receives a license from the             \n");
		sbLicense.append("original licensor to copy, distribute or modify the Program subject to        \n");
		sbLicense.append("these terms and conditions.  You may not impose any further                   \n");
		sbLicense.append("restrictions on the recipients' exercise of the rights granted herein.        \n");
		sbLicense.append("You are not responsible for enforcing compliance by third parties to          \n");
		sbLicense.append("this License.                                                                 \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("  7. If, as a consequence of a court judgment or allegation of patent         \n");
		sbLicense.append("infringement or for any other reason (not limited to patent issues),          \n");
		sbLicense.append("conditions are imposed on you (whether by court order, agreement or           \n");
		sbLicense.append("otherwise) that contradict the conditions of this License, they do not        \n");
		sbLicense.append("excuse you from the conditions of this License.  If you cannot                \n");
		sbLicense.append("distribute so as to satisfy simultaneously your obligations under this        \n");
		sbLicense.append("License and any other pertinent obligations, then as a consequence you        \n");
		sbLicense.append("may not distribute the Program at all.  For example, if a patent              \n");
		sbLicense.append("license would not permit royalty-free redistribution of the Program by        \n");
		sbLicense.append("all those who receive copies directly or indirectly through you, then         \n");
		sbLicense.append("the only way you could satisfy both it and this License would be to           \n");
		sbLicense.append("refrain entirely from distribution of the Program.                            \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("If any portion of this section is held invalid or unenforceable under         \n");
		sbLicense.append("any particular circumstance, the balance of the section is intended to        \n");
		sbLicense.append("apply and the section as a whole is intended to apply in other                \n");
		sbLicense.append("circumstances.                                                                \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("It is not the purpose of this section to induce you to infringe any           \n");
		sbLicense.append("patents or other property right claims or to contest validity of any          \n");
		sbLicense.append("such claims; this section has the sole purpose of protecting the              \n");
		sbLicense.append("integrity of the free software distribution system, which is                  \n");
		sbLicense.append("implemented by public license practices.  Many people have made               \n");
		sbLicense.append("generous contributions to the wide range of software distributed              \n");
		sbLicense.append("through that system in reliance on consistent application of that             \n");
		sbLicense.append("system; it is up to the author/donor to decide if he or she is willing        \n");
		sbLicense.append("to distribute software through any other system and a licensee cannot         \n");
		sbLicense.append("impose that choice.                                                           \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("This section is intended to make thoroughly clear what is believed to         \n");
		sbLicense.append("be a consequence of the rest of this License.                                 \n");
		sbLicense.append("                                                                             \n");
		sbLicense.append("  8. If the distribution and/or use of the Program is restricted in           \n");
		sbLicense.append("certain countries either by patents or by copyrighted interfaces, the         \n");
		sbLicense.append("original copyright holder who places the Program under this License           \n");
		sbLicense.append("may add an explicit geographical distribution limitation excluding            \n");
		sbLicense.append("those countries, so that distribution is permitted only in or among           \n");
		sbLicense.append("countries not thus excluded.  In such case, this License incorporates         \n");
		sbLicense.append("the limitation as if written in the body of this License.                     \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("  9. The Free Software Foundation may publish revised and/or new versions     \n");
		sbLicense.append("of the General Public License from time to time.  Such new versions will      \n");
		sbLicense.append("be similar in spirit to the present version, but may differ in detail to      \n");
		sbLicense.append("address new problems or concerns.                                             \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("Each version is given a distinguishing version number.  If the Program        \n");
		sbLicense.append("specifies a version number of this License which applies to it and \"any       \n");
		sbLicense.append("later version\", you have the option of following the terms and conditions     \n");
		sbLicense.append("either of that version or of any later version published by the Free          \n");
		sbLicense.append("Software Foundation.  If the Program does not specify a version number of     \n");
		sbLicense.append("this License, you may choose any version ever published by the Free Software  \n");
		sbLicense.append("Foundation.                                                                   \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("  10. If you wish to incorporate parts of the Program into other free         \n");
		sbLicense.append("programs whose distribution conditions are different, write to the author     \n");
		sbLicense.append("to ask for permission.  For software which is copyrighted by the Free         \n");
		sbLicense.append("Software Foundation, write to the Free Software Foundation; we sometimes      \n");
		sbLicense.append("make exceptions for this.  Our decision will be guided by the two goals       \n");
		sbLicense.append("of preserving the free status of all derivatives of our free software and     \n");
		sbLicense.append("of promoting the sharing and reuse of software generally.                     \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("			    NO WARRANTY                                         \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("  11. BECAUSE THE PROGRAM IS LICENSED FREE OF CHARGE, THERE IS NO WARRANTY    \n");
		sbLicense.append("FOR THE PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW.  EXCEPT WHEN      \n");
		sbLicense.append("OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER PARTIES        \n");
		sbLicense.append("PROVIDE THE PROGRAM \"AS IS\" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED    \n");
		sbLicense.append("OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF          \n");
		sbLicense.append("MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.  THE ENTIRE RISK AS     \n");
		sbLicense.append("TO THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU.  SHOULD THE        \n");
		sbLicense.append("PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL NECESSARY SERVICING,      \n");
		sbLicense.append("REPAIR OR CORRECTION.                                                         \n");
		sbLicense.append("                                                                              \n");
		sbLicense.append("  12. IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING   \n");
		sbLicense.append("WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MAY MODIFY AND/OR           \n");
		sbLicense.append("REDISTRIBUTE THE PROGRAM AS PERMITTED ABOVE, BE LIABLE TO YOU FOR DAMAGES,    \n");
		sbLicense.append("INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL DAMAGES ARISING   \n");
		sbLicense.append("OUT OF THE USE OR INABILITY TO USE THE PROGRAM (INCLUDING BUT NOT LIMITED     \n");
		sbLicense.append("TO LOSS OF DATA OR DATA BEING RENDERED INACCURATE OR LOSSES SUSTAINED BY      \n");
		sbLicense.append("YOU OR THIRD PARTIES OR A FAILURE OF THE PROGRAM TO OPERATE WITH ANY OTHER    \n");
		sbLicense.append("PROGRAMS), EVEN IF SUCH HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE         \n");
		sbLicense.append("POSSIBILITY OF SUCH DAMAGES.                                                  \n");
		
		strLicense = sbLicense.toString();
		
		JEditorPane taAbout = new JEditorPane("text/html", strAbout);
		JTextArea taLicense = new JTextArea(strLicense);
		JEditorPane ta3rdParty = new JEditorPane("text/html", str3rdParty);
		JEditorPane taThanks = new JEditorPane("text/html", strThanks);
		
		taAbout.setEditable(false);
		taLicense.setEditable(false);
		ta3rdParty.setEditable(false);
		taThanks.setEditable(false);
		
		taAbout.setBorder(new EmptyBorder(5,5,5,5));
		taLicense.setBorder(new EmptyBorder(5,5,5,5));
		ta3rdParty.setBorder(new EmptyBorder(5,5,5,5));
		taThanks.setBorder(new EmptyBorder(5,5,5,5));
		
		//taAbout.setFont(new Font("SansSerif",Font.BOLD,12));
		taLicense.setFont(new Font("Monospaced",Font.PLAIN,12));
		//ta3rdParty.setFont(new Font("Monospaced",Font.PLAIN,14));
		//taThanks.setFont(new Font("SansSerif",Font.BOLD,14));
		
		paneAbout = new JScrollPane(taAbout);
		paneLicense = new JScrollPane(taLicense);
		pane3rdParty = new JScrollPane(ta3rdParty);
		paneThanks = new JScrollPane(taThanks);
		
		tabbedPane.addTab("About",paneAbout);
		tabbedPane.addTab("License",paneLicense);
		tabbedPane.addTab("Acknowledgements",pane3rdParty);
		tabbedPane.addTab("Thanks",paneThanks);
		
		JPanel panel = new JPanel();
		panel.add(buttonOK);
		buttonOK.addActionListener(this);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tabbedPane,BorderLayout.CENTER);
		getContentPane().add(panel,BorderLayout.SOUTH);
		getContentPane().add(new ImagePane(new ImageIcon(ClassLoader.getSystemResource("jpatch/images/jpatch.png"))),BorderLayout.WEST);
		setSize(640,480);
		setLocationRelativeTo(owner);
		setVisible(true);
	}
	
	public static void main(String[] args) {
		new About(null);
	}
	
	class ImagePane extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private ImageIcon icon;
		private ImagePane(ImageIcon icon) {
			this.icon = icon;
			Dimension dim = new Dimension(icon.getIconWidth() + 10,icon.getIconHeight() + 10);
			setPreferredSize(dim);
		}
		
		public void paint(Graphics g) {
			icon.paintIcon(this, g,5,getHeight() - icon.getIconHeight() - 5);
		}
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		if (actionEvent.getSource() == buttonOK) {
			setVisible(false);
			dispose();
		}
	}
}
