varying vec3 normal;

void main() {
//	vec3 pert = vec3(sin(normal.x * 100.0), sin(normal.y * 100.0), sin(normal.z * 100.0));
//	vec3 norm = normalize(normal + 0.1 * pert);
	
	vec3 norm = normalize(normal);
	vec4 color = gl_FrontMaterial.emission + gl_FrontMaterial.ambient * gl_LightModel.ambient;
	for (int i = 0; i < 3; i++) {
		color += gl_LightSource[i].ambient * gl_FrontMaterial.ambient;
		color += max(dot(norm, gl_LightSource[i].position.xyz), 0.0) * gl_LightSource[i].diffuse * gl_FrontMaterial.diffuse;
		vec3 halfVec = normalize(gl_LightSource[i].halfVector.xyz);
   		color += pow(max(dot(norm, halfVec), 0.0), gl_FrontMaterial.shininess) * gl_LightSource[i].specular * gl_FrontMaterial.specular;	
	}
	color.w = 1.0;
	gl_FragColor = color;
}