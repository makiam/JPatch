varying vec3 normal;
//	varying vec3 pos;

void main() {
	gl_Position = ftransform();
	normal = normalize(gl_NormalMatrix * gl_Normal);
//	pos = gl_Vertex.xyz;
}