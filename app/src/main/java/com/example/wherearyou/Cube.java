package com.example.wherearyou;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/// 직사각형 객체 그래픽 정보
public class Cube {
    private static final String TAG = Cube.class.getSimpleName();

    // vertex 쉐이더 코드
    private final String vertexShaderString =
            "attribute vec3 aPosition;\n" +
                    "attribute vec4 aColor;\n" +
                    "uniform mat4 uMvpMatrix; \n" +
                    "varying vec4 vColor;\n" +
                    "void main() {\n" +
                    "  vColor = aColor;\n" +
                    "  gl_Position = uMvpMatrix * vec4(aPosition.x, aPosition.y, aPosition.z, 1.0);\n" +
                    "}";

    // fragment 쉐이더 코드
    private final String fragmentShaderString =
            "precision mediump float;\n" +
                    "varying vec4 vColor;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = vColor;\n" +
                    "}";

    private int mProgram;

    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjMatrix = new float[16];

    private FloatBuffer mVertices;
    private FloatBuffer mColors;
    private ShortBuffer mIndices;

    // 정점 배열
    private float vertices[] = {
            -0.5f, 0.0f, -0.5f,
            0.5f, 0.0f, -0.5f,
            0.5f, 1.0f, -0.5f,
            -0.5f, 1.0f, -0.5f,
            -0.5f, 0.0f,  0.5f,
            0.5f, 0.0f,  0.5f,
            0.5f, 1.0f,  0.5f,
            -0.5f, 1.0f,  0.5f
    };
    // 인덱스 배열
    private short indices[] = {
            0, 5, 4, 0, 1, 5,
            1, 6, 5, 1, 2, 6,
            2, 7, 6, 2, 3, 7,
            4, 6, 7, 4, 5, 6,
            3, 1, 0, 3, 2, 1,
            0, 4, 3, 4, 3, 7
    };

    public static final int RED = 0;
    public static final int GREEN = 1;
    public static final int BLUE = 2;
    public static final int ALPHA = 3;
    private float[] mColor = new float[]{0.0f, 0.0f, 0.0f, 1.0f};

    public Cube(float scale, int color, float alpha) {
        float[] scaledVertices = new float[3 * 8];
        for (int i = 0; i < 3 * 8; i++) {
            scaledVertices[i] = vertices[i] * scale;
        }

        mColor[RED] = Color.red(color) / 255.f;
        mColor[GREEN] = Color.green(color) / 255.f;
        mColor[BLUE] = Color.blue(color) / 255.f;
        mColor[ALPHA] = Color.alpha(color) / 255.f;

        float[] colors = new float[4 * 8];
        for (int i = 0; i < 8 ; i++) {
            colors[4 * i + 0] = mColor[RED];
            colors[4 * i + 1] = mColor[GREEN];
            colors[4 * i + 2] = mColor[BLUE];
            colors[4 * i + 3] = alpha;
        }

        // ByteBuffer를 할당 받아 사용할 엔디안을 지정하고 FloatBuffer로 변환
        mVertices = ByteBuffer.allocateDirect(vertices.length * Float.SIZE / 8).order(ByteOrder.nativeOrder()).asFloatBuffer();
        // float 배열에 정의된 좌표들을 FloatBuffer에 저장
        mVertices.put(scaledVertices);
        // 읽어올 버퍼의 위치를 0으로 설정. 첫번째 좌표부터 읽어오게됨.
        mVertices.position(0);

        mColors = ByteBuffer.allocateDirect(colors.length * Float.SIZE / 8).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mColors.put(colors);
        mColors.position(0);

        mIndices = ByteBuffer.allocateDirect(indices.length * Short.SIZE / 8).order(ByteOrder.nativeOrder()).asShortBuffer();
        mIndices.put(indices);
        mIndices.position(0);
    }

    public void init() {
        // vertex 쉐이더 컴파일
        int vShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vShader, vertexShaderString);
        GLES20.glCompileShader(vShader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(vShader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile vertex shader.");
            GLES20.glDeleteShader(vShader);
        }

        // fragment 쉐이더 컴파일
        int fShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fShader, fragmentShaderString);
        GLES20.glCompileShader(fShader);
        GLES20.glGetShaderiv(fShader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile fragment shader.");
            GLES20.glDeleteShader(fShader);
        }

        mProgram = GLES20.glCreateProgram();
        // vertex 쉐이더와 fragment 쉐이더를 program 객체에 추가
        GLES20.glAttachShader(mProgram, vShader);
        GLES20.glAttachShader(mProgram, fShader);
        // program 객체를 OpenGL에 연결. program에 추가된 쉐이더들이 OpenGL에 연결됨.
        GLES20.glLinkProgram(mProgram);
        int[] linked = new int[1];
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linked, 0);
        if (linked[0] == 0) {
            Log.e(TAG, "Could not link program.");
        }
    }

    public void draw() {
        Log.d("TAG", "Cube draw 함수 시작");
        // 렌더링 상태의 일부분으로 program을 추가한다.
        GLES20.glUseProgram(mProgram);

        // program 객체로부터 vertex 쉐이더의 aPosition 멤버에 대한 핸들을 가져옴
        int position = GLES20.glGetAttribLocation(mProgram, "aPosition");
        // program 객체로부터 fragment 쉐이더의 aColor 멤버에 대한 핸들을 가져옴
        int color = GLES20.glGetAttribLocation(mProgram, "aColor");
        int mvp = GLES20.glGetUniformLocation(mProgram, "uMvpMatrix");

        float[] mvMatrix = new float[16];
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mProjMatrix, 0, mvMatrix, 0);

        GLES20.glUniformMatrix4fv(mvp, 1, false, mvpMatrix, 0);

        // 메모리 위반 용의자!!!
        GLES20.glEnableVertexAttribArray(position);
        GLES20.glVertexAttribPointer(position, 3, GLES20.GL_FLOAT, false, 4 * 3, mVertices);

        GLES20.glEnableVertexAttribArray(color);
        GLES20.glVertexAttribPointer(color, 4, GLES20.GL_FLOAT, false, 4 * 4, mColors);

        Log.d("TAG", "오류 발생 지점");
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 36, GLES20.GL_UNSIGNED_SHORT, mIndices);
        Log.d("TAG", "오류 발생 지점 넘어감");

        GLES20.glDisableVertexAttribArray(position);
        Log.d("TAG", "Cube draw 함수 끝");
    }

    public void setModelMatrix(float[] modelMatrix) {
        System.arraycopy(modelMatrix, 0, mModelMatrix, 0, 16);
    }

    public void setProjectionMatrix(float[] projMatrix) {
        System.arraycopy(projMatrix, 0, mProjMatrix, 0, 16);
    }

    public void setViewMatrix(float[] viewMatrix) {
        System.arraycopy(viewMatrix, 0, mViewMatrix, 0, 16);
    }
}